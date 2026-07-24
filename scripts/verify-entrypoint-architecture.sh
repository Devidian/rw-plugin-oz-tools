#!/usr/bin/env bash
set -euo pipefail
descriptor="src/resources/plugin.yml"; source_root="src"
fail() { echo "entry-point architecture: $*" >&2; exit 1; }
[[ -f "$descriptor" ]] || fail "missing $descriptor"
main_class="$(sed -n 's/^main:[[:space:]]*//p' "$descriptor" | tr -d '\r' | head -n 1)"
[[ -n "$main_class" ]] || fail "plugin.yml does not declare main"
entry_file="$source_root/${main_class//./\/}.java"
[[ -f "$entry_file" ]] || fail "entry source not found: $entry_file"
mapfile -t java_files < <(find "$source_root" -type f -name '*.java' -print | sort)
listener_count=0
for file in "${java_files[@]}"; do
    if grep -Eq 'implements[^{]*(^|[ ,])Listener([ ,{]|$)|implements[^{]*net\.risingworld\.api\.events\.Listener' "$file"; then
        if grep -Eq 'import[[:space:]]+net\.risingworld\.api\.events\.Listener;' "$file" || grep -Eq 'net\.risingworld\.api\.events\.Listener' "$file"; then
            listener_count=$((listener_count + 1)); [[ "$file" == "$entry_file" ]] || fail "Rising World Listener outside entry class: $file"
        fi
    fi
    if [[ "$file" != "$entry_file" ]] && grep -Eq '^[[:space:]]*@EventMethod([[:space:]]*\\([^)]*\\))?[[:space:]]*$' "$file"; then fail "@EventMethod outside entry class: $file"; fi
    while IFS= read -r registration; do
        [[ "$registration" =~ registerEventListener[[:space:]]*\([[:space:]]*this[[:space:]]*\) ]] || fail "listener registration must target this: $file: $registration"
        [[ "$file" == "$entry_file" ]] || fail "listener registration outside entry class: $file"
    done < <(grep -E 'registerEventListener[[:space:]]*\(' "$file" || true)
done
[[ "$listener_count" -eq 1 ]] || fail "expected exactly one Rising World Listener, found $listener_count"
forbidden='(^|[^[:alnum:]_])(for|while|switch)[[:space:]]*\(|\.split[[:space:]]*\(|DriverManager|execute(Query|Update)?[[:space:]]*\(|new[[:space:]]+UI[A-Z]|new[[:space:]]+Timer|scheduleAtFixedRate|executeDelayed[[:space:]]*\('
if grep -En "$forbidden" "$entry_file"; then fail "entry class contains workflow, persistence, UI, or timer logic"; fi
echo "entry-point architecture: OK ($main_class)"
