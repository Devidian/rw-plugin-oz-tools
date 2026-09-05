# JSON-only distribution

- Remove legacy settings defaults and translation properties assets; ship their existing JSON replacements only.
- Restrict resource copying to JSON and exclude stale legacy settings/translation files from ZIP assembly.
- Preserve existing-server settings migration and backups. No runtime data, dependency, or version changes.
