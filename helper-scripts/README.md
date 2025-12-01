# Plugin Download Scripts

This repository contains small scripts that download the latest release asset of a plugin from GitHub Releases and (optionally) extract it.

Included scripts

- `download-latest-plugin.sh` — Bash script to download and extract the latest release asset for a single plugin (owner/repo + PluginBaseName).
- `download-multiple-plugins.sh` — Wrapper that processes a list of (repo,plugin) lines and calls `download-latest-plugin.sh` for each entry. Optionally performs a final extraction and removal pass for `.zip` files.
- `download-latest-plugin.ps1` — PowerShell version (for Windows, optional).

Requirements (for the Bash scripts)

- bash
- curl
- jq
- unzip (for .zip extraction)
- tar (optional, for tarball extraction)

Install missing tools, for example on Debian/Ubuntu:

```bash
sudo apt update
sudo apt install -y curl jq unzip tar
```

Make scripts executable

```bash
chmod +x download-latest-plugin.sh
chmod +x download-multiple-plugins.sh
```

Authentication / rate limits

- Optionally set a Personal Access Token if you need to access private repositories or want higher GitHub API limits:

```bash
export GITHUB_TOKEN=ghp_...
```

Usage — Single plugin (Bash)

```bash
# Syntax:
./download-latest-plugin.sh owner/repo PluginBaseName [OutDir]

# Example: download the latest OZDiscordConnect .zip into the directory where the script is located:
./download-latest-plugin.sh Devidian/rw-plugin-oz-discord-connect OZDiscordConnect .

# Or: download into a server directory "Plugins" (e.g. when the script is located in your deploy folder)
./download-latest-plugin.sh Devidian/rw-plugin-oz-discord-connect OZDiscordConnect Plugins
```

Behavior for OutDir

- If you specify `.` or `./` as the `OutDir`, the target directory is treated as the directory where the script resides — no additional subfolders for repo/plugin are created.
- If you provide a different `OutDir` (e.g. `./releases`), the script places the assets and extracted files in that directory (when using the wrapper, it may create a structure like `outdir/owner_repo/PluginName`, unless `--flat` is used).

Usage — Multiple plugins (wrapper)

1. Create a file `plugins.txt` with one plugin per line in this format:

```txt
owner/repo,PluginBaseName
```

Example:

```txt
Devidian/rw-plugin-oz-discord-connect,OZDiscordConnect
otherowner/some-repo,SomePlugin
```

2. Run the wrapper:

```bash
# Syntax:
./download-multiple-plugins.sh list-file [outdir-base] [--flat] [--no-extract] [--remove-zips]

# Examples:
#  - Default: places each plugin under ./releases/<owner_repo>/<Plugin>
./download-multiple-plugins.sh plugins.txt

#  - Target directly into server directory "Plugins" (if the script is in the deploy folder)
./download-multiple-plugins.sh plugins.txt Plugins

#  - If you copy the scripts directly into the server Plugins directory:
#    use '.' (or ./) as outdir — files will land directly inside the Plugins directory
./download-multiple-plugins.sh plugins.txt . --flat

#  - Flat mode (no nested folders)
./download-multiple-plugins.sh plugins.txt ./ --flat

#  - Remove ZIPs after extraction:
./download-multiple-plugins.sh plugins.txt ./ --flat --remove-zips

#  - Skip extraction:
./download-multiple-plugins.sh plugins.txt ./ --no-extract
```

Wrapper flags

- `--flat`  
  Saves downloaded files directly into the specified `outdir-base` (no extra subdirectories).
- `--no-extract`  
  Skips the final extraction pass (ZIPs are only downloaded).
- `--remove-zips`  
  Deletes ZIP files after successful extraction. Only applied when extraction was performed and succeeded.

PowerShell (Windows)

- Use `download-latest-plugin.ps1` with:

```powershell
.\download-latest-plugin.ps1 -Repo 'Devidian/rw-plugin-oz-discord-connect' -PluginName 'OZDiscordConnect' -OutDir '.'
```

- The PowerShell script uses `Invoke-RestMethod` / `Invoke-WebRequest` and `Expand-Archive`. For tarballs it attempts to use `tar` if available.

Error handling / troubleshooting

- "No matching asset found" — asset pattern not found. Check the asset names/releases on GitHub or adjust the PluginBaseName (assets must start with `PluginBaseName-`).
- API rate limit / 403 — set `GITHUB_TOKEN` or wait for the rate limit to reset.
- `jq` or `unzip` missing — install the required dependencies (see above).
- Permission errors when writing — check filesystem permissions and ensure the script/user has write access to the target directory.

Tips

- If you copy the scripts to your server's Plugins directory, use `OutDir` or run the wrapper with `.`/`--flat` so ZIPs and extracted files are placed directly in the correct directory.
- If multiple plugins contain files with the same names, extracting may overwrite files. Test first without `--remove-zips` and consider using per-plugin OutDirs if you want to avoid collisions.

License / redistribution

- You are free to use and adapt the scripts. If you redistribute them, it’s nice (but not required) to mention their origin.

Notice:

- README and scripts were created using GPT-5 mini (GitHub)
