"""Progress dashboard generated from docs/PLAN.md.

docs/PLAN.md is the only source of truth; this script only renders a view of it.
Writes docs/dashboard/index.html (not in git) and, on the first run, downloads the
pinned Mermaid library to docs/dashboard/vendor/ (not in git) for dependency graphs.

Usage:
    python .claude/scripts/plan_dashboard.py [--plan PATH] [--out DIR] [--offline]

The format of PLAN.md is defined in .claude/skills/plan-interview/SKILL.md.
Exit code 1 if the plan has problems (they are also shown on the page).
"""

from __future__ import annotations

import argparse
import hashlib
import html
import json
import re
import sys
import urllib.request
from dataclasses import dataclass, field
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from req_index import REPO, parse_requirements  # noqa: E402

PLAN = REPO / "docs" / "PLAN.md"
OUT = REPO / "docs" / "dashboard"

MERMAID_VERSION = "12.3.0"
MERMAID_URL = f"https://cdn.jsdelivr.net/npm/mermaid@{MERMAID_VERSION}/dist/mermaid.min.js"
MERMAID_SHA256 = "336df803d87a38b70a01e736e666c8f7601d2ce42f5e47c8fafe5d721074b9a3"

STATUSES = ("todo", "in progress", "done", "dropped")
POINTS = {"S": 1, "M": 2, "L": 4, "XL": 8}

_MILESTONE = re.compile(r"^## M(\d+)\s*[-–—]\s*(.+)$")
_H2 = re.compile(r"^## ")
_TASK_ROW = re.compile(r"^\|\s*(T\d+\.\d+)\s*\|")
_FIELD = re.compile(r"^\*\*(Goal|Manual test):\*\*\s*(.*)")
_ID = re.compile(r"\b(?:BHV|REQ)-[A-Z]+(?:-R?\d+[a-z]?|\d+[a-z]?)\b")


@dataclass
class Task:
    id: str
    title: str
    status: str
    effort: str
    deps: list[str]
    covers: list[str]
    milestone: int
    line: int
    note: str = ""


@dataclass
class Milestone:
    num: int
    name: str
    goal: str = ""
    manual_test: str = ""
    tasks: list[Task] = field(default_factory=list)


@dataclass
class Plan:
    title: str = "Plan"
    status: str = ""
    summary: str = ""
    milestones: list[Milestone] = field(default_factory=list)
    deferred: list[str] = field(default_factory=list)
    problems: list[str] = field(default_factory=list)


def _split_list(cell: str) -> list[str]:
    cell = cell.strip()
    if cell in ("", "-", "–", "—"):
        return []
    return [p.strip() for p in cell.split(",") if p.strip()]


def _plain(text: str) -> str:
    return re.sub(r"\*\*|`", "", text).strip()


def parse_plan(path: Path) -> Plan:
    plan = Plan()
    lines = path.read_text(encoding="utf-8").splitlines()
    current: Milestone | None = None
    section = ""
    summary: list[str] = []
    last_field = None
    for n, raw in enumerate(lines, 1):
        line = raw.rstrip()
        if line.startswith("# Plan:"):
            plan.title = line[2:].strip()
            continue
        if line.startswith("> **STATUS") and not plan.status:
            plan.status = _plain(line.lstrip("> "))
            continue
        if m := _MILESTONE.match(line):
            current = Milestone(int(m.group(1)), m.group(2).strip())
            plan.milestones.append(current)
            section, last_field = "milestone", None
            continue
        if _H2.match(line):
            current, last_field = None, None
            section = line[3:].strip().lower()
            continue
        if section == "summary" and line.strip():
            summary.append(line.strip())
        elif section == "deferred":
            plan.deferred += [i for i in _ID.findall(line) if i not in plan.deferred]
        elif current is not None:
            if m := _FIELD.match(line):
                last_field = "goal" if m.group(1) == "Goal" else "manual_test"
                setattr(current, last_field, _plain(m.group(2)))
            elif last_field and line.strip() and not line.startswith("|") and not line.startswith("**"):
                setattr(current, last_field, getattr(current, last_field) + " " + _plain(line))
            elif not line.strip() or line.startswith("|"):
                last_field = None
            if m := _TASK_ROW.match(line):
                cells = [c.strip() for c in line.strip().strip("|").split("|")]
                if len(cells) != 6:
                    plan.problems.append(f"line {n}: task row needs 6 columns, has {len(cells)}")
                    continue
                tid, title, status, effort, deps, covers = cells
                current.tasks.append(Task(tid, _plain(title), status.lower(), effort.upper(),
                                          _split_list(deps), _split_list(covers), current.num, n))
            elif (m := re.match(r"^\*\*(T\d+\.\d+):\*\*\s*(.*)", line)):
                for t in current.tasks:
                    if t.id == m.group(1):
                        t.note = _plain(m.group(2))
    plan.summary = " ".join(summary)
    return plan


def validate(plan: Plan, req_ids: dict) -> None:
    tasks = {}
    for ms in plan.milestones:
        for t in ms.tasks:
            where = f"{t.id} (line {t.line})"
            if t.id in tasks:
                plan.problems.append(f"{where}: duplicate task ID")
            tasks[t.id] = t
            if not t.id.startswith(f"T{ms.num}."):
                plan.problems.append(f"{where}: ID does not match milestone M{ms.num}")
            if t.status not in STATUSES:
                plan.problems.append(f"{where}: unknown status '{t.status}'")
            if t.effort not in POINTS:
                plan.problems.append(f"{where}: unknown effort '{t.effort}'")
            for c in t.covers:
                if c not in req_ids:
                    plan.problems.append(f"{where}: covers unknown requirement {c}")
                elif not req_ids[c].needs_coverage:
                    plan.problems.append(f"{where}: covers {c} ({req_ids[c].priority}), not a v1 requirement")
    for t in tasks.values():
        for d in t.deps:
            if d not in tasks:
                plan.problems.append(f"{t.id}: depends on unknown task {d}")
            elif tasks[d].status == "dropped" and t.status != "dropped":
                plan.problems.append(f"{t.id}: depends on dropped task {d}")
            elif t.status == "done" and tasks[d].status != "done":
                plan.problems.append(f"{t.id}: done, but its dependency {d} is not")
    # Cycles (depth-first search).
    state: dict[str, int] = {}

    def visit(tid: str, stack: list[str]) -> None:
        state[tid] = 1
        for d in tasks[tid].deps:
            if d not in tasks:
                continue
            if state.get(d) == 1:
                cyc = stack[stack.index(d):] + [d] if d in stack else [tid, d]
                plan.problems.append("dependency cycle: " + " -> ".join(cyc))
            elif d not in state:
                visit(d, stack + [d])
        state[tid] = 2

    for tid in tasks:
        if tid not in state:
            visit(tid, [tid])
    for d in plan.deferred:
        if d not in req_ids:
            plan.problems.append(f"Deferred: unknown requirement {d}")


def ensure_mermaid(out: Path, offline: bool) -> str | None:
    """Returns a warning, or None when the library is in place."""
    target = out / "vendor" / "mermaid.min.js"
    if target.exists() and hashlib.sha256(target.read_bytes()).hexdigest() == MERMAID_SHA256:
        return None
    if offline:
        return "Mermaid is missing (offline run); dependency graphs show as lists."
    try:
        data = urllib.request.urlopen(MERMAID_URL, timeout=60).read()
    except OSError as e:
        return f"Mermaid download failed ({e}); dependency graphs show as lists."
    if hashlib.sha256(data).hexdigest() != MERMAID_SHA256:
        return "Mermaid download has an unexpected checksum; not used."
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(data)
    return None


def build_data(plan: Plan, reqs: list) -> dict:
    req_ids = {r.id: r for r in reqs}
    tasks = [t for ms in plan.milestones for t in ms.tasks]
    return {
        "title": plan.title,
        "status": plan.status,
        "summary": plan.summary,
        "generated": datetime.now().strftime("%Y-%m-%d %H:%M"),
        "points": POINTS,
        "problems": plan.problems,
        "milestones": [
            {"num": ms.num, "name": ms.name, "goal": ms.goal, "manualTest": ms.manual_test,
             "tasks": [t.id for t in ms.tasks]}
            for ms in plan.milestones
        ],
        "tasks": {
            t.id: {"title": t.title, "status": t.status, "effort": t.effort, "deps": t.deps,
                   "covers": t.covers, "milestone": t.milestone, "note": t.note}
            for t in tasks
        },
        "reqs": [
            {"id": r.id, "priority": r.priority, "section": r.section, "title": r.title,
             "kind": r.kind}
            for r in reqs if r.needs_coverage or any(r.id in t.covers for t in tasks)
        ],
        "deferred": [d for d in plan.deferred if d in req_ids],
    }


def render(data: dict, has_mermaid: bool) -> str:
    template = (Path(__file__).resolve().parent / "plan_dashboard.html").read_text(encoding="utf-8")
    payload = json.dumps(data, ensure_ascii=False).replace("</", "<\\/")
    mermaid_tag = '<script src="vendor/mermaid.min.js"></script>' if has_mermaid else ""
    return (template
            .replace("{{TITLE}}", html.escape(data["title"]))
            .replace("{{MERMAID}}", mermaid_tag)
            .replace("{{DATA}}", payload))


def terminal_summary(data: dict) -> list[str]:
    tasks = {k: v for k, v in data["tasks"].items() if v["status"] != "dropped"}
    total = sum(POINTS.get(t["effort"], 0) for t in tasks.values())
    done = sum(POINTS.get(t["effort"], 0) for t in tasks.values() if t["status"] == "done")
    pct = round(100 * done / total) if total else 0
    ready = [k for k, t in tasks.items() if t["status"] == "todo"
             and all(data["tasks"].get(d, {}).get("status") == "done" for d in t["deps"])]
    doing = [k for k, t in tasks.items() if t["status"] == "in progress"]
    return [f"Progress: {done}/{total} points ({pct}%), "
            f"{sum(t['status'] == 'done' for t in tasks.values())}/{len(tasks)} tasks done",
            f"In progress: {', '.join(doing) or '-'}",
            f"Ready: {', '.join(ready[:5]) or '-'}"]


def main() -> int:
    sys.stdout.reconfigure(encoding="utf-8")
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--plan", type=Path, default=PLAN)
    ap.add_argument("--out", type=Path, default=OUT)
    ap.add_argument("--offline", action="store_true", help="do not download Mermaid")
    args = ap.parse_args()
    if not args.plan.exists():
        print(f"No plan file: {args.plan}", file=sys.stderr)
        return 1
    reqs = parse_requirements()
    plan = parse_plan(args.plan)
    validate(plan, {r.id: r for r in reqs})
    args.out.mkdir(parents=True, exist_ok=True)
    warning = ensure_mermaid(args.out, args.offline)
    data = build_data(plan, reqs)
    (args.out / "index.html").write_text(render(data, warning is None), encoding="utf-8")
    for line in terminal_summary(data):
        print(line)
    if warning:
        print("Warning: " + warning)
    for p in plan.problems:
        print("Problem: " + p)
    print(f"Written: {args.out / 'index.html'}")
    return 1 if plan.problems else 0


if __name__ == "__main__":
    sys.exit(main())
