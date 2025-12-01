#!/usr/bin/env bash
# Wrapper: read a simple CSV (repo,plugin) per line and download each plugin using download-latest-plugin.sh
# Then, optionally, extract all .zip files found under the output base and optionally remove the zip files.
#
# File format (comma-separated, no header):
# Devidian/rw-plugin-oz-discord-connect,OZDiscordConnect
# owner2/repo2,PluginBaseName2
#
# Usage:
#   ./download-multiple-plugins.sh list-file [outdir-base] [--flat] [--no-extract] [--remove-zips]
# Examples:
#   ./download-multiple-plugins.sh plugins.txt ./all-releases
#   ./download-multiple-plugins.sh plugins.txt ./ --flat
#   ./download-multiple-plugins.sh plugins.txt ./ --no-extract
#   ./download-multiple-plugins.sh plugins.txt ./ --remove-zips
set -euo pipefail

print_usage() {
  echo "Usage: $0 list-file [outdir-base] [--flat] [--no-extract] [--remove-zips]" >&2
  exit 2
}

if [ "$#" -lt 1 ]; then
  print_usage
fi

# Positional and optional flags parsing
LIST_FILE=""
OUTDIR_BASE="./releases"
FLAT=false
EXTRACT=true
REMOVE_ZIPS=false

LIST_FILE="$1"
shift || true

# process remaining args
while [ "$#" -gt 0 ]; do
  case "$1" in
    --flat)
      FLAT=true
      shift
      ;;
    --no-extract)
      EXTRACT=false
      shift
      ;;
    --remove-zips)
      REMOVE_ZIPS=true
      shift
      ;;
    -*)
      echo "Unknown option: $1" >&2
      print_usage
      ;;
    *)
      # first non-flag positional becomes OUTDIR_BASE (if not set by user)
      OUTDIR_BASE="$1"
      shift
      # allow more flags after this
      ;;
  esac
done

if [ ! -f "$LIST_FILE" ]; then
  echo "List file not found: $LIST_FILE" >&2
  exit 3
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DOWNLOAD_SCRIPT="$SCRIPT_DIR/download-latest-plugin.sh"
if [ ! -x "$DOWNLOAD_SCRIPT" ]; then
  echo "download-latest-plugin.sh not found or not executable in $SCRIPT_DIR" >&2
  echo "Please place download-latest-plugin.sh next to this wrapper and make it executable." >&2
  exit 4
fi

# Normalize OUTDIR_BASE: if user passed "." or "./" treat as script dir and enable flat
if [ "$OUTDIR_BASE" = "." ] || [ "$OUTDIR_BASE" = "./" ]; then
  OUTDIR_BASE="$SCRIPT_DIR"
  FLAT=true
fi

mkdir -p "$OUTDIR_BASE"

while IFS= read -r line || [ -n "$line" ]; do
  # skip empty and comment lines
  line="${line%%#*}"   # strip comments
  line="$(echo -n "$line" | tr -d '\r\n' | xargs)" # trim whitespace
  if [ -z "$line" ]; then
    continue
  fi

  IFS=',' read -r repo plugin <<< "$line"
  repo="$(echo -n "$repo" | xargs)"
  plugin="$(echo -n "$plugin" | xargs)"
  if [ -z "$repo" ] || [ -z "$plugin" ]; then
    echo "Skipping invalid line: $line" >&2
    continue
  fi

  if [ "$FLAT" = true ]; then
    target_outdir="$OUTDIR_BASE"
  else
    target_outdir="$OUTDIR_BASE/$(echo "$repo" | tr '/' '_')/$plugin"
  fi

  echo "=== Downloading plugin '$plugin' from repo '$repo' into $target_outdir ==="
  "$DOWNLOAD_SCRIPT" "$repo" "$plugin" "$target_outdir"
done < "$LIST_FILE"

# Final extraction pass: find *.zip and extract them if unzip is available
if [ "$EXTRACT" = true ]; then
  echo "=== Final extraction pass: searching for .zip files under $OUTDIR_BASE ==="
  if ! command -v unzip >/dev/null 2>&1; then
    echo "Warning: 'unzip' not found. Skipping final extraction. Install unzip to enable extraction." >&2
  else
    # choose find args: if FLAT then non-recursive, otherwise recursive
    if [ "$FLAT" = true ]; then
      find_args=( "$OUTDIR_BASE" -maxdepth 1 -type f -name '*.zip' -print0 )
    else
      find_args=( "$OUTDIR_BASE" -type f -name '*.zip' -print0 )
    fi

    found=false
    while IFS= read -r -d '' zipfile; do
      found=true
      zipdir="$(dirname "$zipfile")"
      echo "Extracting '$zipfile' -> '$zipdir' ..."
      if unzip -o "$zipfile" -d "$zipdir"; then
        echo "Extraction succeeded: '$zipfile'"
        if [ "$REMOVE_ZIPS" = true ]; then
          echo "Removing zip: '$zipfile'"
          rm -f -- "$zipfile"
        fi
      else
        echo "Warning: extraction failed for '$zipfile' (zip left in place)" >&2
      fi
    done < <(find "${find_args[@]}")

    if [ "$found" = false ]; then
      echo "No .zip files found to extract."
    else
      echo "Extraction pass completed."
    fi
  fi
else
  echo "Final extraction pass skipped (--no-extract)."
fi

# Summary if zips were removed
if [ "$REMOVE_ZIPS" = true ]; then
  echo "Note: --remove-zips was enabled; successfully extracted zips were deleted."
fi

echo "All done."