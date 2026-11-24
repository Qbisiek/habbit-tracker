#!/usr/bin/env bash
# Mechanical reference check of the project documents.
# Run from anywhere: bash .claude/scripts/check-refs.sh
# Exit code 0 = no problem found, 1 = problems listed on stdout.
#
# Checks (in docs/*.md and README.md):
#   1. every BHV-* and REQ-* reference has a definition in docs/REQUIREMENTS.md
#      (a bold rule "**ID" or the first cell of a table row "| ID |"); no ID is
#      defined twice;
#   2. every "Decision N" / "Decisions N, M" / "Dec. N" has a row in
#      docs/DESIGN-DECISIONS.md; ranges like "1-39" are descriptions and are skipped;
#      no Decision number is defined twice;
#   3. every UI-* ID uses a screen from the closed list of docs/UX.md section 1 and
#      is defined in docs/UX.md (it appears there);
#   4. every "`docs/X.md` section(s) N" reference points to an existing numbered
#      heading of that file.
# Not checked: short forms after a full ID ("BHV-ENTRY-R6a, R6b", "BHV-ENTRY-21a, 21b").

set -u
export LC_ALL=C

root="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$root" || exit 2

files=(docs/*.md README.md)
req=docs/REQUIREMENTS.md
dec=docs/DESIGN-DECISIONS.md
ux=docs/UX.md
screens='NAV|DAY|ENTRIES|ENTRYFORM|HABITS|HABITFORM|STATS|IMPORT|DIALOG|MSG|NOTIF'

problems=0
report() { # title, then lines on stdin; prints only when there are lines.
  # Called at the end of a pipe (a subshell), so a problem is marked in a file.
  local title="$1" body
  body="$(cat)"
  if [ -n "$body" ]; then
    echo "== $title"
    echo "$body" | sed 's/^/  /'
    : > "$flag"
  fi
}
flag="$(mktemp -u)"
trap 'rm -f "$flag"' EXIT

# 1. BHV and REQ
for prefix in BHV REQ; do
  defs="$( { grep -ohE "\*\*$prefix-[A-Z]+-?[A-Z]*[0-9]+[a-z]?" "$req" | sed 's/^\*\*//';
             grep -ohE "^\| $prefix-[A-Z]+-?[A-Z]*[0-9]+[a-z]? \|" "$req" | sed 's/^| //; s/ |$//'; } )"
  echo "$defs" | sort | uniq -d | report "$prefix defined twice"
  grep -ohE "$prefix-[A-Z]+-?[A-Z]*[0-9]+[a-z]?" "${files[@]}" | sort -u \
    | comm -23 - <(echo "$defs" | sort -u) | report "$prefix referenced but not defined"
done

# 2. Decisions
dec_defs="$(grep -oE '^\| [0-9]+ \|' "$dec" | tr -dc '0-9\n')"
echo "$dec_defs" | sort | uniq -d | report "Decision defined twice"
grep -ohE '(Decisions?|Dec\.) [0-9]+(-[0-9]+)?(( *, *| and | to )[0-9]+(-[0-9]+)?)*' "${files[@]}" \
  | sed -E 's/[0-9]+-[0-9]+//g' | grep -oE '[0-9]+' | sort -u \
  | comm -23 - <(echo "$dec_defs" | sort -u) | report "Decision referenced but not defined"

# 3. UI identifiers
ui_refs="$(grep -ohE 'UI-[A-Z]+(-[A-Z]+)*' "${files[@]}" | sort -u)"
echo "$ui_refs" | grep -vE "^UI-($screens)(-|$)" | report "UI ID with a screen not in the closed list"
ui_defs="$(grep -ohE 'UI-[A-Z]+(-[A-Z]+)*' "$ux" | sort -u)"
comm -23 <(echo "$ui_refs") <(echo "$ui_defs") \
  | report "UI ID used but not defined in docs/UX.md"

# 4. Section references to other documents
grep -oHE '`docs/[A-Z-]+\.md` sections? [0-9]+(\.[0-9]+)*(( *, *| and )[0-9]+(\.[0-9]+)*)*' "${files[@]}" \
  | while IFS= read -r hit; do
      src="${hit%%:*}"
      ref="${hit#*:}"
      target="$(echo "$ref" | grep -oE 'docs/[A-Z-]+\.md' | head -1)"
      nums="$(echo "${ref#*\` }" | grep -oE '[0-9]+(\.[0-9]+)*')"
      if [ ! -f "$target" ]; then echo "$src: $target does not exist"; continue; fi
      for n in $nums; do
        grep -qE "^#{2,4} $n[ .]" "$target" || echo "$src: $target section $n not found"
      done
    done | sort -u | report "Section reference to a missing heading"

if [ -e "$flag" ]; then
  problems=1
else
  echo "OK: all references resolved."
fi
exit "$problems"
