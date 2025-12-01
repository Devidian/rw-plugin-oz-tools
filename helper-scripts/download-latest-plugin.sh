#!/usr/bin/env bash
# Download the latest release asset for a given plugin name from a GitHub repo and extract it.
# Usage: download-latest-plugin.sh owner/repo PluginBaseName [OutDir]
# Example: ./download-latest-plugin.sh Devidian/rw-plugin-oz-discord-connect OZDiscordConnect ./
set -euo pipefail

if [ "$#" -lt 2 ]; then
  echo "Usage: $0 owner/repo PluginBaseName [OutDir]" >&2
  exit 2
fi

REPO="$1"                  # owner/repo
PLUGIN_NAME="$2"           # e.g. OZDiscordConnect
OUTDIR="${3:-./release}"   # default output directory
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
API_URL="https://api.github.com/repos/$REPO/releases/latest"

# If user passed "." or "./" treat it as the directory where the script resides
if [ "$OUTDIR" = "." ] || [ "$OUTDIR" = "./" ]; then
  OUTDIR="$SCRIPT_DIR"
fi

# Optional: set GITHUB_TOKEN env var to increase rate limits or access private repos
CURL_AUTH_OPTS=()
if [ -n "${GITHUB_TOKEN:-}" ]; then
  CURL_AUTH_OPTS+=(-H "Authorization: token $GITHUB_TOKEN")
fi

# Check dependencies
if ! command -v curl >/dev/null 2>&1; then
  echo "Error: curl is required." >&2
  exit 3
fi
if ! command -v jq >/dev/null 2>&1; then
  echo "Error: jq is required." >&2
  exit 4
fi

echo "Querying latest release for $REPO ..."
json=$(curl -s "${CURL_AUTH_OPTS[@]}" "$API_URL")
if [ -z "$json" ]; then
  echo "Error: empty response from GitHub API" >&2
  exit 5
fi

# Build regex like ^PLUGIN_NAME-.* (match files starting with PluginName-)
PATTERN="^${PLUGIN_NAME}-.*"
# Get all matching assets (name and url)
mapfile -t assets < <(echo "$json" | jq -r --arg pat "$PATTERN" '.assets[] | select(.name | test($pat)) | "\(.name) \(.browser_download_url)"')

if [ "${#assets[@]}" -eq 0 ]; then
  echo "No matching asset found for plugin pattern: $PATTERN" >&2
  echo "Available assets:" >&2
  echo "$json" | jq -r '.assets[].name' >&2
  exit 6
fi

# Prefer .zip, then .tar.gz/.tgz, then first found
chosen_name=""
chosen_url=""
for entry in "${assets[@]}"; do
  name="${entry%% *}"
  url="${entry#* }"
  if [[ "$name" == *.zip ]]; then
    chosen_name="$name"
    chosen_url="$url"
    break
  fi
  if [[ -z "$chosen_name" ]]; then
    chosen_name="$name"
    chosen_url="$url"
  fi
done

if [ -z "$chosen_url" ]; then
  echo "Failed to select an asset." >&2
  exit 7
fi

mkdir -p "$OUTDIR"
outfile="$OUTDIR/$chosen_name"

echo "Downloading $chosen_name ..."
# Use auth header if provided
if [ -n "${GITHUB_TOKEN:-}" ]; then
  curl -L -f -H "Authorization: token $GITHUB_TOKEN" -o "$outfile" "$chosen_url"
else
  curl -L -f -o "$outfile" "$chosen_url"
fi

echo "Saved to $outfile"

# Extract based on extension (extraction happens in the same OUTDIR)
case "$chosen_name" in
  *.zip)
    if command -v unzip >/dev/null 2>&1; then
      echo "Extracting zip to $OUTDIR ..."
      unzip -o "$outfile" -d "$OUTDIR"
    else
      echo "unzip not found; skipping extraction. File available at: $outfile"
    fi
    ;;
  *.tar.gz|*.tgz|*.tar.xz|*.tar.bz2|*.tar)
    if command -v tar >/dev/null 2>&1; then
      echo "Extracting tarball to $OUTDIR ..."
      tar -xf "$outfile" -C "$OUTDIR"
    else
      echo "tar not found; skipping extraction. File available at: $outfile"
    fi
    ;;
  *)
    echo "Unknown archive format; skipping automatic extraction. File available at: $outfile"
    ;;
esac

echo "Done. Contents available under: $OUTDIR"