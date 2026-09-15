# I18n runtime reload

## Objective

Refresh a plugin's JSON translation catalogue when that plugin is initialized again at the same installation path.

## Ownership and dependencies

- Owner: `rw-plugin-oz-tools`; consumer: `rw-plugin-maven-template`.
- No new dependencies, persistence changes, or public API changes.

## Checklist

- [x] Identify the path-based i18n cache as retaining stale catalogue entries after plugin reload.
- [x] Replace the catalogue atomically during plugin initialization and cover the same-path reload case.
- [x] Build Tools and Maven Template, then upload both to Development.
- [ ] Confirm `RELOADED ALL PLUGINS` and resolved Template PluginSettings labels in-game.

## Risks, validation and rollback

- Risk: malformed replacement catalogues must not partially replace an already-loaded catalogue.
- Validation: unit test, Maven package/test, packaged-resource check, and Development runtime check.
- Rollback: restore the previous Tools and Template JARs; plugin settings and data are retained.
