<#
Download the latest release asset for a given plugin name from a GitHub repo and extract it.
Usage:
  .\download-latest-plugin.ps1 -Repo 'owner/repo' -PluginName 'OZDiscordConnect' [-OutDir '.\release']
  Use OutDir '.' to place files in the directory where the script resides.
Example:
  .\download-latest-plugin.ps1 -Repo 'Devidian/rw-plugin-oz-discord-connect' -PluginName 'OZDiscordConnect' -OutDir '.'
#>
param(
  [Parameter(Mandatory=$true)][string]$Repo,
  [Parameter(Mandatory=$true)][string]$PluginName,
  [string]$OutDir = ".\release"
)

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# Treat "." or "./" as script directory
if ($OutDir -eq "." -or $OutDir -eq "./") {
  $OutDir = $ScriptDir
}

$ApiUrl = "https://api.github.com/repos/$Repo/releases/latest"
$Pattern = "^$PluginName-.*"

$Headers = @{}
if ($env:GITHUB_TOKEN) {
  $Headers.Authorization = "token $($env:GITHUB_TOKEN)"
}

Write-Host "Querying latest release for $Repo ..."
try {
  $release = Invoke-RestMethod -Uri $ApiUrl -Headers $Headers -ErrorAction Stop
}
catch {
  Write-Error "Failed to query GitHub API: $_"
  exit 1
}

$matching = $release.assets | Where-Object { $_.name -match $Pattern }
if (-not $matching) {
  Write-Error "No matching asset found for plugin pattern: $Pattern"
  Write-Host "Available assets:"
  $release.assets | ForEach-Object { Write-Host $_.name }
  exit 2
}

# Prefer .zip if present
$asset = $matching | Where-Object { $_.name -like '*.zip' } | Select-Object -First 1
if (-not $asset) {
  $asset = $matching | Select-Object -First 1
}

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$outfile = Join-Path (Resolve-Path $OutDir) $asset.name

Write-Host "Downloading $($asset.name) ..."
try {
  Invoke-WebRequest -Uri $asset.browser_download_url -OutFile $outfile -Headers $Headers -UseBasicParsing -ErrorAction Stop
}
catch {
  Write-Error "Download failed: $_"
  exit 3
}

# Extract
if ($outfile -like '*.zip') {
  Write-Host "Extracting zip to $OutDir ..."
  try {
    Expand-Archive -LiteralPath $outfile -DestinationPath $OutDir -Force
  }
  catch {
    Write-Error "Expand-Archive failed: $_"
  }
}
elseif ($outfile -like '*.tar.gz' -or $outfile -like '*.tgz' -or $outfile -like '*.tar.*' -or $outfile -like '*.tar') {
  Write-Host "Attempting to extract tarball to $OutDir using 'tar'..."
  if (Get-Command tar -ErrorAction SilentlyContinue) {
    & tar -xf $outfile -C $OutDir
  } else {
    Write-Warning "tar not available on this system; file saved at: $outfile"
  }
}
else {
  Write-Host "Unknown archive format. File saved at: $outfile"
}

Write-Host "Done. Contents available under: $OutDir"