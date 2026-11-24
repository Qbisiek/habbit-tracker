"""Index of requirement IDs in docs/REQUIREMENTS.md.

Prints one line per ID: ID | priority | title, grouped by section. Used by the
plan-interview skill instead of reading the whole requirements file, and imported
by plan-dashboard.py for the coverage of requirements.

Usage:
    python .claude/scripts/req_index.py              # all IDs
    python .claude/scripts/req_index.py BHV-EVAL     # IDs starting with a prefix
    python .claude/scripts/req_index.py --coverage   # only IDs that need coverage

Recognized forms (anything else is ignored):
    - **REQ-M1** Text...              bullets in sections 2-4 (also nested, REQ-M8a)
    | REQ-X1 | Title | ... |          table rows in section 6
    - **BHV-EVAL-R1 Title:** ...      rules in section 7
    | BHV-EVAL-1 | Given | When | ... scenario rows in section 7
"""

from __future__ import annotations

import re
import sys
from dataclasses import dataclass, field
from pathlib import Path

REPO = Path(__file__).resolve().parents[2]
REQUIREMENTS = REPO / "docs" / "REQUIREMENTS.md"

# Priorities whose IDs must be covered by a task of the plan or deferred by the user.
COVERAGE_PRIORITIES = ("Must", "Should", "Constraint", "Success", "Behavior")

TITLE_MAX = 100

_H2 = re.compile(r"^## (\d+)\.\s+(.*)")
_H3 = re.compile(r"^### (.*)")
_REQ_BULLET = re.compile(r"^\s*- \*\*(REQ-[A-Z]+\d+[a-z]?)\*\*\s*(.*)")
_REQ_ROW = re.compile(r"^\|\s*(REQ-[A-Z]+\d+[a-z]?)\s*\|([^|]*)\|")
_BHV_RULE = re.compile(r"^- \*\*(BHV-[A-Z]+-R\d+[a-z]?)\s+([^*]+?):?\*\*")
_BHV_ROW = re.compile(r"^\|\s*(BHV-[A-Z]+-\d+[a-z]?)\s*\|")
_MAP_ROW = re.compile(r"^\|\s*(7\.\d+)\s*\|[^|]*\|[^|]*\|([^|]*)\|")


@dataclass
class Req:
    id: str
    priority: str  # Must, Should, Won't, Constraint, Success, Extensibility, Behavior
    section: str  # e.g. "Must", "7.1 Evaluation of a goal period"
    title: str
    kind: str  # req, rule, scenario
    related: list[str] = field(default_factory=list)  # REQ IDs of a BHV section

    @property
    def needs_coverage(self) -> bool:
        return self.priority in COVERAGE_PRIORITIES


def _short(text: str) -> str:
    text = re.sub(r"\s+", " ", text).strip()
    text = text.replace("**", "").replace("`", "")
    # End of the first sentence; "e.g." and "i.e." do not end it.
    m = re.search(r"(?<!\be\.g)(?<!\bi\.e)(?<=[a-z0-9)])\. ", text)
    if m:
        text = text[: m.start()]
    text = text.rstrip(".:;")
    if len(text) > TITLE_MAX:
        text = text[: TITLE_MAX - 3].rstrip() + "..."
    return text


def _priority(h2_num: str, h3: str) -> str | None:
    if h2_num == "2":
        if h3.startswith("Must"):
            return "Must"
        if h3.startswith("Should"):
            return "Should"
        if h3.startswith("Won't"):
            return "Won't"
        return None
    return {"3": "Constraint", "4": "Success", "6": "Extensibility", "7": "Behavior"}.get(h2_num)


def _map_related(lines: list[str]) -> dict[str, list[str]]:
    """Section number (7.x) -> related REQ IDs, from the "Map" table."""
    related: dict[str, list[str]] = {}
    for line in lines:
        m = _MAP_ROW.match(line)
        if m:
            short = [s.strip() for s in m.group(2).split(",") if s.strip()]
            related[m.group(1)] = [f"REQ-{s}" for s in short if re.fullmatch(r"[A-Z]+\d+[a-z]?", s)]
    return related


def parse_requirements(path: Path = REQUIREMENTS) -> list[Req]:
    lines = path.read_text(encoding="utf-8").splitlines()
    related_by_section = _map_related(lines)
    reqs: list[Req] = []
    h2_num, h2_title, h3 = "", "", ""
    i = 0
    while i < len(lines):
        line = lines[i]
        if m := _H2.match(line):
            h2_num, h2_title, h3 = m.group(1), m.group(2).strip(), ""
        elif m := _H3.match(line):
            h3 = m.group(1).strip()
        prio = _priority(h2_num, h3)
        if prio is None:
            i += 1
            continue
        if h2_num == "7":
            section = h3 or h2_title
            sec_num = section.split(" ", 1)[0]
            related = related_by_section.get(sec_num, [])
        else:
            section = h3 if h2_num == "2" else h2_title
            related = []

        if m := _REQ_BULLET.match(line):
            text = m.group(2)
            indent = len(line) - len(line.lstrip())
            j = i + 1
            while j < len(lines):
                nxt = lines[j]
                if not nxt.strip() or nxt.lstrip().startswith("- ") or nxt.startswith("#"):
                    break
                if len(nxt) - len(nxt.lstrip()) <= indent:
                    break
                text += " " + nxt.strip()
                j += 1
            reqs.append(Req(m.group(1), prio, section, _short(text), "req"))
        elif m := _REQ_ROW.match(line):
            reqs.append(Req(m.group(1), prio, section, _short(m.group(2)), "req"))
        elif m := _BHV_RULE.match(line):
            reqs.append(Req(m.group(1), prio, section, _short(m.group(2)), "rule", related))
        elif m := _BHV_ROW.match(line):
            cells = [c.strip() for c in line.strip().strip("|").split("|")]
            when = cells[2] if len(cells) > 2 else ""
            reqs.append(Req(m.group(1), prio, section, _short(when), "scenario", related))
        i += 1
    return reqs


def main(argv: list[str]) -> int:
    sys.stdout.reconfigure(encoding="utf-8")
    coverage_only = "--coverage" in argv
    prefixes = [a for a in argv if not a.startswith("--")]
    reqs = parse_requirements()
    seen: set[str] = set()
    dupes = sorted({r.id for r in reqs if r.id in seen or seen.add(r.id)})
    if dupes:
        print("Duplicate IDs: " + ", ".join(dupes), file=sys.stderr)
    if coverage_only:
        reqs = [r for r in reqs if r.needs_coverage]
    if prefixes:
        reqs = [r for r in reqs if any(r.id.startswith(p) for p in prefixes)]
    print(f"# Requirement IDs from docs/REQUIREMENTS.md ({len(reqs)}): ID | priority | title")
    print("# Scenario titles are the When column. Read a rule or a row in the file when needed.")
    current = None
    for r in reqs:
        if r.section != current:
            current = r.section
            rel = f" (related: {', '.join(r.related)})" if r.related else ""
            print(f"\n## {current}{rel}")
        print(f"{r.id} | {r.priority} | {r.title}")
    return 1 if dupes else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
