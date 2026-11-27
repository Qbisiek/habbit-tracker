"""Tasks of one milestone by state, for the implementation loop.

docs/PLAN.md is the only source of truth. A task is ready when its status is `todo`
and every task in `Depends on` is `done`. The loop takes only ready tasks.

Usage:
    python .claude/scripts/plan_ready.py M2 [--plan PATH]

Output (one line each, IDs separated by commas, `-` when empty):
    ready, in progress, blocked, waiting (todo with open dependencies), done.
Exit code 1 if the milestone does not exist or the plan has problems.
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from plan_dashboard import PLAN, parse_plan, validate  # noqa: E402
from req_index import parse_requirements  # noqa: E402


def main() -> int:
    sys.stdout.reconfigure(encoding="utf-8")
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("milestone", help="e.g. M2")
    ap.add_argument("--plan", type=Path, default=PLAN)
    args = ap.parse_args()
    num = int(args.milestone.upper().lstrip("M"))

    plan = parse_plan(args.plan)
    validate(plan, {r.id: r for r in parse_requirements()})
    status = {t.id: t.status for ms in plan.milestones for t in ms.tasks}
    milestone = next((ms for ms in plan.milestones if ms.num == num), None)
    if milestone is None:
        print(f"No milestone M{num} in {args.plan}", file=sys.stderr)
        return 1

    groups: dict[str, list[str]] = {k: [] for k in
                                    ("ready", "in progress", "blocked", "waiting", "done")}
    for t in milestone.tasks:
        if t.status == "todo":
            open_deps = [d for d in t.deps if status.get(d) != "done"]
            groups["waiting" if open_deps else "ready"].append(t.id)
        elif t.status in groups:
            groups[t.status].append(t.id)
    for name, ids in groups.items():
        print(f"{name}: {', '.join(ids) or '-'}")
    for p in plan.problems:
        print("Problem: " + p)
    return 1 if plan.problems else 0


if __name__ == "__main__":
    sys.exit(main())
