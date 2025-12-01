# Plugin Download Scripts

Dieses Repository enthält kleine Skripte, mit denen du die jeweils neueste Release‑Asset eines Plugins aus GitHub‑Releases herunterladen und (optional) entpacken kannst.

Enthaltene Skripte

- `download-latest-plugin.sh` — Bash‑Skript zum Herunterladen und Entpacken des neuesten Release‑Assets für ein einzelnes Plugin (owner/repo + PluginBaseName).
- `download-multiple-plugins.sh` — Wrapper, der eine Liste von (repo,plugin)-Zeilen verarbeitet und für jedes Element `download-latest-plugin.sh` aufruft. Führt am Ende optional eine Extraktions- und Entfernen‑Phase für `.zip`-Dateien aus.
- `download-latest-plugin.ps1` — PowerShell‑Version (für Windows, optional).

Voraussetzungen (für Bash‑Skripte)

- bash
- curl
- jq
- unzip (für .zip‑Extraktion)
- tar (optional, für tarball‑Extraktion)

Installiere fehlende Tools z. B. auf Debian/Ubuntu:

```bash
sudo apt update
sudo apt install -y curl jq unzip tar
```

Skripte ausführbar machen

```bash
chmod +x download-latest-plugin.sh
chmod +x download-multiple-plugins.sh
```

Authentifizierung / Rate limits

- Optional kannst du ein Personal Access Token setzen, wenn du private Repositories nutzen willst oder höhere GitHub API‑Limits brauchst:

```bash
export GITHUB_TOKEN=ghp_...
```

Usage — Einzelnes Plugin (Bash)

```bash
# Syntax:
./download-latest-plugin.sh owner/repo PluginBaseName [OutDir]

# Beispiel: lade neueste OZDiscordConnect .zip in das Verzeichnis, in dem das Skript liegt:
./download-latest-plugin.sh Devidian/rw-plugin-oz-discord-connect OZDiscordConnect .

# Oder: lade in ein Server-Verzeichnis "Plugins" (z.B. wenn du Skript im Deploy-Ordner liegen hast)
./download-latest-plugin.sh Devidian/rw-plugin-oz-discord-connect OZDiscordConnect Plugins
```

Verhalten bei OutDir

- Wenn du als `OutDir` `.` oder `./` angibst, wird das Zielverzeichnis in das Verzeichnis geändert, in dem das Skript liegt — es werden keine zusätzlichen Unterordner für Repo/Plugin erstellt.
- Wenn du ein anderes `OutDir` angibst (z. B. `./releases`), legt das Skript die Assets und entpackten Dateien in dieses Verzeichnis (bei Verwendung des Wrappers kann zusätzlich eine Struktur `outdir/owner_repo/PluginName` entstehen, sofern nicht --flat gesetzt ist).

Usage — Mehrere Plugins (Wrapper)

1. Erstelle eine Datei `plugins.txt` mit einer Zeile pro Plugin im Format:

```txt
owner/repo,PluginBaseName
```

Beispiel:

```txt
Devidian/rw-plugin-oz-discord-connect,OZDiscordConnect
otherowner/some-repo,SomePlugin
```

2. Aufruf des Wrappers:

```bash
# Syntax:
./download-multiple-plugins.sh list-file [outdir-base] [--flat] [--no-extract] [--remove-zips]

# Beispiele:
#  - Standard: legt jede Plugin-Ausgabe unter ./releases/<owner_repo>/<Plugin> ab
./download-multiple-plugins.sh plugins.txt

#  - Ziel direkt in Server-Verzeichnis Plugins (wenn Skript z.B. in Deploy-Ordner liegt)
./download-multiple-plugins.sh plugins.txt Plugins

#  - Wenn du die Skripte direkt in das Server-Plugins-Verzeichnis kopierst:
#    dann nutze als outdir "." (oder ./) — die Dateien landen direkt im Plugins‑Verzeichnis
./download-multiple-plugins.sh plugins.txt . --flat

#  - Flat mode (keine Unterordner)
./download-multiple-plugins.sh plugins.txt ./ --flat

#  - Nach dem Extrahieren ZIPs löschen:
./download-multiple-plugins.sh plugins.txt ./ --flat --remove-zips

#  - Extraktion überspringen:
./download-multiple-plugins.sh plugins.txt ./ --no-extract
```

Flags des Wrappers

- `--flat`  
  Speichert die heruntergeladenen Dateien direkt im angegebenen `outdir-base` (keine zusätzlichen Unterordner).
- `--no-extract`  
  Unterdrückt die finale Extraktions‑Phase (die ZIPs werden nur heruntergeladen).
- `--remove-zips`  
  Löscht ZIP‑Dateien nach erfolgreichem Entpacken. Wird nur angewandt, wenn die Extraktion erfolgreich durchführbar war.

PowerShell (Windows)

- Nutze `download-latest-plugin.ps1` mit:

```powershell
.\download-latest-plugin.ps1 -Repo 'Devidian/rw-plugin-oz-discord-connect' -PluginName 'OZDiscordConnect' -OutDir '.'
```

- PowerShell‑Skript verwendet `Invoke-RestMethod`/`Invoke-WebRequest` und `Expand-Archive`. Für tarballs versucht es `tar`, falls vorhanden.

Fehlerbehandlung / Troubleshooting

- "No matching asset found" — Asset‑Pattern nicht gefunden. Prüfe Namen/Release auf GitHub oder passe PluginBaseName an (Assets müssen mit `PluginBaseName-` beginnen).
- API rate limit / 403 — setze `GITHUB_TOKEN` oder warte, bis der Rate Limit zurückgesetzt ist.
- `jq` oder `unzip` fehlt — installiere die Abhängigkeiten (siehe oben).
- Zugriffsfehler beim Schreiben — prüfe Dateisystemrechte und ob das Skript/Benutzer Schreibrechte im Zielverzeichnis hat.

Tipps

- Wenn du die Skripte in dein Server‑Plugins‑Verzeichnis kopierst, setze `OutDir` bzw. rufe den Wrapper mit `.`/`--flat` auf — so landen ZIPs und entpackte Dateien direkt im richtigen Verzeichnis.
- Wenn mehrere Plugins gleichnamige Dateien enthalten, kann das Entpacken Dateien überschreiben. Teste zuerst ohne `--remove-zips` und ggf. mit individuellem OutDir je Plugin.

Lizenz / Weitergabe

- Du kannst die Skripte frei verwenden und anpassen. Wenn du sie weitergibst, gib bitte an, dass sie ursprünglich von diesem Projekt stammen (keine Pflicht, nur nett).

Hinweis

- Diese Anleitung und scripte wurden mit GPT-5 mini erstellt (GitHub)
