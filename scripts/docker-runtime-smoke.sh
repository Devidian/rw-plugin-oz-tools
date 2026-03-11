#!/usr/bin/env bash
set -euo pipefail

SERVER_ROOT="${RW_SERVER_ROOT:-/appdata/rising-world/dedicated-server}"
PLUGINS_DIR="${RW_PLUGINS_DIR:-$SERVER_ROOT/Plugins}"
LOGS_DIR="${RW_LOGS_DIR:-$SERVER_ROOT/Logs}"

usage() {
  cat <<'EOF'
Usage:
  scripts/docker-runtime-smoke.sh <PluginFolderName>

Environment:
  RW_SERVER_ROOT  Override Rising World server root.
  RW_PLUGINS_DIR  Override plugin deployment directory.
  RW_LOGS_DIR     Override log directory.

Expected local build outputs:
  - dist/<PluginFolderName>/

This script copies the built plugin directory into the configured server plugin
directory and prints the newest log files for manual smoke-test inspection.
EOF
}

main() {
  [[ $# -eq 1 ]] || { usage; exit 1; }

  local plugin_folder="$1"
  local dist_dir="dist/$plugin_folder"
  local target_dir="$PLUGINS_DIR/$plugin_folder"

  if [[ ! -d "$dist_dir" ]]; then
    echo "Build output not found: $dist_dir" >&2
    exit 1
  fi

  mkdir -p "$PLUGINS_DIR"
  rm -rf "$target_dir"
  cp -R "$dist_dir" "$target_dir"

  echo "Deployed plugin folder:"
  echo "  source: $dist_dir"
  echo "  target: $target_dir"
  echo
  echo "Current plugin files:"
  find "$target_dir" -maxdepth 3 -type f | sort
  echo

  if [[ -d "$LOGS_DIR" ]]; then
    echo "Latest Rising World log files:"
    find "$LOGS_DIR" -maxdepth 1 -type f | sort | tail -n 5
  else
    echo "Log directory not found: $LOGS_DIR"
  fi
}

main "$@"
