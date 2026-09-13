# Next 130926b: Plugin reinstall and uninstall

## Objective

Add administrator-confirmed reinstall and uninstall actions to the OZ Tools
release-notes page for installed compatible plugins with a GitHub release URL.
Reinstall restores the latest packaged files while retaining persisted plugin
data. Uninstall moves the complete plugin directory to a hidden, non-loaded
rollback directory and then reloads plugins.

## Ownership and dependencies

- Owner: `rw-plugin-oz-tools` (release package handling, admin UI, runtime
  reload coordination, translations, and tests).
- Affected runtime plugins: all installed plugins whose `plugin.yml` declares
  a compatible GitHub release URL; no feature-plugin source changes.
- Dependencies: existing trusted-release allow-list, Java NIO staging/move
  operations, existing server-thread dispatcher and `reloadplugins` command.

## Risks and rollback

- Both actions are administrator-only and require an explicit confirmation.
- Reinstall uses the established staged package replacement and preserves
  settings, JSON data, and SQLite data. A failed stage leaves the live plugin
  intact.
- Uninstall never deletes files: it atomically moves the directory to
  `Plugins/.oz-uninstalled/`, which is the manual rollback location. Windows
  keeps the existing loaded-file safety restriction.
- A reload is requested only after a successful operation.

## Validation strategy

- Unit-test the reinstall/uninstall file operations and trust boundaries.
- Run the repository Maven tests and package build.
- Deploy only OZ Tools to `rw-server-dev`, then verify upload and plugin reload
  evidence in Development logs. Player interaction remains a manual admin test.

## Checklist

- [x] Inspect the existing update service, release-notes UI, trust policy, and
  Development upload path.
- [x] Add safe reinstall and reversible uninstall service operations.
- [x] Add confirmed admin UI actions and German/English player text.
- [x] Add focused tests and update behavior documentation/changelog.
- [x] Build, test, and upload only OZ Tools to Development (31 tests passed;
  Development reloaded all plugins on 2026-09-13).
