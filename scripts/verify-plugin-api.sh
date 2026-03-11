#!/usr/bin/env bash
set -euo pipefail

API_JAR="${PLUGIN_API_JAR:-libs/PluginAPI.jar}"

usage() {
  cat <<'EOF'
Usage:
  scripts/verify-plugin-api.sh --summary
  scripts/verify-plugin-api.sh --class <fully.qualified.ClassName>
  scripts/verify-plugin-api.sh --method <fully.qualified.ClassName#methodName>
  scripts/verify-plugin-api.sh --file <path>

File format:
  - empty lines and lines starting with # are ignored
  - class checks:  fully.qualified.ClassName
  - method checks: fully.qualified.ClassName#methodName

Environment:
  PLUGIN_API_JAR overrides the default JAR path (libs/PluginAPI.jar).
EOF
}

require_api_jar() {
  if [[ ! -f "$API_JAR" ]]; then
    echo "PluginAPI JAR not found: $API_JAR" >&2
    exit 1
  fi
}

class_exists() {
  local class_name="$1"
  local class_path="${class_name//./\/}.class"
  jar tf "$API_JAR" | grep -Fqx "$class_path"
}

method_exists() {
  local class_name="$1"
  local method_name="$2"
  javap -classpath "$API_JAR" "$class_name" 2>/dev/null | grep -Eq "[[:space:]]${method_name}\\("
}

check_symbol() {
  local symbol="$1"

  if [[ "$symbol" == *"#"* ]]; then
    local class_name="${symbol%%#*}"
    local method_name="${symbol#*#}"

    if method_exists "$class_name" "$method_name"; then
      echo "OK    method  $symbol"
    else
      echo "FAIL  method  $symbol" >&2
      return 1
    fi
  else
    if class_exists "$symbol"; then
      echo "OK    class   $symbol"
    else
      echo "FAIL  class   $symbol" >&2
      return 1
    fi
  fi
}

check_file() {
  local file_path="$1"
  local failed=0

  if [[ ! -f "$file_path" ]]; then
    echo "Symbol file not found: $file_path" >&2
    exit 1
  fi

  while IFS= read -r line || [[ -n "$line" ]]; do
    line="${line#"${line%%[![:space:]]*}"}"
    line="${line%"${line##*[![:space:]]}"}"
    [[ -z "$line" || "${line:0:1}" == "#" ]] && continue
    check_symbol "$line" || failed=1
  done < "$file_path"

  return "$failed"
}

main() {
  require_api_jar

  if [[ $# -eq 0 ]]; then
    usage
    exit 1
  fi

  local command="$1"
  shift

  case "$command" in
    --summary)
      echo "PluginAPI JAR: $API_JAR"
      echo "Entries: $(jar tf "$API_JAR" | wc -l)"
      ;;
    --class)
      [[ $# -eq 1 ]] || { usage; exit 1; }
      check_symbol "$1"
      ;;
    --method)
      [[ $# -eq 1 ]] || { usage; exit 1; }
      check_symbol "$1"
      ;;
    --file)
      [[ $# -eq 1 ]] || { usage; exit 1; }
      check_file "$1"
      ;;
    *)
      usage
      exit 1
      ;;
  esac
}

main "$@"
