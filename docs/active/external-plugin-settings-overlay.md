# External plugin settings overlay

## Objective

Show every installed non-OZ plugin in the OZ Tools settings overlay, including
its declared version, without pretending that it implements OZ Tools panels.

## Ownership and dependencies

`rw-plugin-oz-tools` owns installed-plugin discovery, the shared overlay, and
release-source parsing. No feature-plugin repository changes are required.
Rising World supplies the installed `Plugin` descriptors; a usable external
release source is a canonical GitHub URL in `plugin.yml` `website`.

## Risks, migration, and rollback

This adds no persistent schema or plugin API migration. External plugins get
only an informational tab and, for administrators with a valid GitHub URL,
the existing release-notes tab. Existing registered OZ panels remain unchanged.
The external repository opt-in continues to gate release checks and installation.

Windows installed-plugin updates are disabled by default. `allowWindowsUpdate`
is an explicit administrator opt-in to use an in-place replacement rather than
renaming the loaded plugin directory: it first copies the complete installed
package-file tree to an ignored staging backup, replaces package files, removes
obsolete package files, and restores the backup if any operation fails. Persistent
settings, JSON data, and SQLite files are excluded from every copy/delete step.
A locked package file can still fail the update safely. Default JSON files are
package content and are refreshed.

## Validation

- [x] Route discovery and UI ownership to OZ Tools.
- [x] Include installed external descriptors and their declared versions.
- [x] Suppress Settings, Saved Data, and PluginSettings for external plugins.
- [x] Add localized external-plugin guidance and condition release notes on a
      valid GitHub release URL.
- [x] Preserve local data while allowing `*.default.json` package updates.
- [x] Run `mvn -B -Dmaven.repo.local=/tmp/oz-tools-external-plugin-m2 test`
      (28 tests) and the matching `-DskipTests package` build.
- [ ] Upload only OZ Tools to `rw-server-dev` and confirm its reload/startup
      log evidence. The attempted SSH deployment requires explicit approval in
      this environment and was not performed.
- [blocked] Local Windows runtime install attempted on 2026-09-08. The running
      server denied replacement-file creation for `OZTools.jar`, i18n, and
      libraries, so the new JAR checksum is not installed. Its obsolete
      `.properties` i18n files were removed by the package cleanup; retained
      `settings.properties`, SQLite data, and JSON configuration were not
      changed. Stop the local server before a retry, then verify its startup.
- [x] Install the validated package into the stopped local Dedicated Server at
      `J:\SteamM2\steamapps\common\RisingWorldDedicatedServer`. WSL file
      replacement is denied by its NTFS bridge even while stopped, so the
      package was copied through the local Windows user instead. The installed
      JAR SHA-256 and both JSON translations match the package. The subsequent
      2026-09-08 startup loaded `OZTools.jar` 0.24.1, registered its listener,
      loaded `de.json` and `en.json`, and confirmed the PluginAPI timer on the
      server thread.
- [x] Install external `RespawnChest` 1.1.0 from its public GitHub release as
      a fresh local Dedicated Server plugin. Its release checksum matched and
      the watcher reloaded all plugins, loaded `RespawnChest.jar`, created
      `refill.db`, registered its listener, and logged `enabled`. The upstream
      descriptor has no `website`, so this is the expected no-release-notes
      external-plugin case.
- [x] Add the default-disabled `allowWindowsUpdate` administrator opt-in. It
      removes only the Windows preflight rejection; the existing staged swap,
      failure handling, and backup rollback remain unchanged.
- [x] Run `mvn -B -Dmaven.repo.local=/tmp/oz-tools-external-plugin-m2 test`
      (29 tests) and the matching `-DskipTests package` build; verify the ZIP
      ships `allowWindowsUpdate: false`.
- [x] Replace the failed Windows directory rename with the opt-in in-place
      update path, including complete file backup, obsolete-package cleanup,
      and rollback. Keep the atomic directory swap on non-Windows platforms.
- [x] Exclude persistent world JSON and SQLite files from the Windows backup,
      update, cleanup, and rollback steps after the live Wallet test proved its
      open database cannot be copied on Windows.
- [x] Keep all Windows rollback files in the hidden `.oz-update-*` staging
      directory after live testing showed a sibling `*.oz-backup` directory is
      discovered and loaded as a duplicate Rising World plugin.
