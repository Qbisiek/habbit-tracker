"""PreToolUse hook: keeps the AI from changing `main` and from force pushes.

Reads the hook JSON from stdin. Exit code 2 blocks the tool call and shows the
reason to the model. The user's own git commands are not affected.

A command is split into steps (`&&`, `||`, `;`, `|`, new lines); a checkout or
switch in one step changes the branch the next steps run on.
"""

import json
import re
import subprocess
import sys

PROTECTED = "main"
GIT = r"\bgit(?:\s+-C\s+\S+)?\s+"
CHANGING = re.compile(GIT + r"(commit|merge|rebase|cherry-pick|reset|revert|pull|am|tag)\b")
SWITCH = re.compile(GIT + r"(?:checkout|switch)\s+(?:-[bBcC]\s+)?(?:--\S+\s+)*([^\s-]\S*)")
PUSH = re.compile(GIT + r"push\b(.*)")
FORCE = re.compile(r"(\s--force(-with-lease|-if-includes)?\b|\s-[a-zA-Z]*f[a-zA-Z]*\b|\s\+\S)")
TARGETS_MAIN = re.compile(r"(^|[\s:/])" + PROTECTED + r"(\s|$)")


def current_branch(cwd):
    try:
        result = subprocess.run(
            ["git", "rev-parse", "--abbrev-ref", "HEAD"],
            cwd=cwd or None, capture_output=True, text=True, timeout=10,
        )
        return result.stdout.strip()
    except (OSError, subprocess.SubprocessError):
        return ""


def steps(command):
    return [s.strip() for s in re.split(r"&&|\|\||[;|\n]", command) if s.strip()]


def has_refspec(push_args):
    """`git push <remote> <refspec>`: a refspec is the second non-option argument.
    `HEAD` means the current branch, so it does not count as an explicit one."""
    args = [a for a in push_args.split() if not a.startswith("-")]
    refspecs = args[1:]
    return bool(refspecs) and "HEAD" not in refspecs


def step_reason(step, branch):
    push = PUSH.search(step)
    if push and FORCE.search(push.group(1)):
        return "Force push is not allowed."
    if push and (TARGETS_MAIN.search(push.group(1)) or (branch == PROTECTED and not has_refspec(push.group(1)))):
        return f"Pushing to '{PROTECTED}' is done by the user only."
    if CHANGING.search(step) and branch == PROTECTED:
        return f"'{PROTECTED}' is changed by the user only (merge of a milestone, tag)."
    return None


def block_reason(command, cwd):
    if "git" not in command:
        return None
    branch = current_branch(cwd)
    for step in steps(command):
        reason = step_reason(step, branch)
        if reason:
            return reason
        switch = SWITCH.search(step)
        if switch:
            branch = switch.group(1)
    return None


def main():
    payload = json.load(sys.stdin)
    command = payload.get("tool_input", {}).get("command", "")
    reason = block_reason(command, payload.get("cwd"))
    if reason:
        print(f"Blocked by .claude/scripts/guard_main.py: {reason} "
              "See android/CODING-GUIDELINES.md section 9.", file=sys.stderr)
        sys.exit(2)


if __name__ == "__main__":
    main()
