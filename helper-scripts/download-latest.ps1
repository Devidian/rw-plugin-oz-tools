param(
  [string]$OutDir = ".\release"
)

# Download the latest OZDiscordConnect-*.zip from Devidian/rw-plugin-oz-discord-connect and extract it
$Repo = "Devidian/rw-plugin-oz-discord-connect"
$ApiUrl = "https://api.github.com/repos/$Repo/releases/latest"
$Pattern = '^OZDiscordConnect-.*\.zip$'

$Headers = @{}
if ($env:GITHUB_TOKEN) {
  $Headers.Authorization = "token $($env:GITHUB_TOKEN)"
}

Write-Host "Abfrage der neuesten Release-Meta für $Repo..."
$release = Invoke-RestMethod -Uri $ApiUrl -Headers $Headers -ErrorAction Stop

$asset = $release.assets | Where-Object { $_.name -match $Pattern } | Select-Object -First 1
if (-not $asset) {
  Write-Error "Kein passendes Asset gefunden für Pattern $Pattern"
  Write-Host "Verfügbare Assets:"
  $release.assets | ForEach-Object { Write-Host $_.name }
  exit 1
}

New-Item -ItemType Directory -Force -Path $OutDir | Out-Null
$outfile = Join-Path (Resolve-Path $OutDir) $asset.name

Write-Host "Lade $($asset.name) herunter..."
Invoke-WebRequest -Uri $asset.browser_download_url -OutFile $outfile -Headers $Headers

# Entpacken (zip)
if ($outfile -like "*.zip") {
  Write-Host "Entpacke zip..."
  Expand-Archive -Path $outfile -DestinationPath $OutDir -Force
} else {
  Write-Host "Unbekanntes Archivformat: $outfile (keine automatische Entpackung durchgeführt)"
}

Write-Host "Fertig. Inhalte liegen in: $OutDir"