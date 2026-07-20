# Next 04: Plugin update management

## Objective

Add an opt-in, delayed GitHub-release check and an administrator-only UI to
manually check, inspect, and install an available public release. The catalogue
also identifies supported OZ plugins that are not installed yet; uninstallation
and selecting older releases stay explicitly deferred.

## Ownership and dependencies

OZ Tools owns release metadata parsing, bounded GitHub HTTP access, version
comparison, safe package staging/replacement, reload coordination, and the
shared settings-overlay UI. Each maintained runtime plugin supplies canonical
repository metadata in `plugin.yml`.

Affected repositories/plugins:

- `rw-plugin-oz-tools`: update service, settings, UI, localization, package
  installation, documentation, and tests.
- Each installable `rw-plugin-oz-*` runtime repository: canonical GitHub
  repository URL in `src/resources/plugin.yml` and release-package metadata if
  the existing filename convention is insufficient.

No feature-plugin business logic moves into Tools. No new external library is
planned; Java's HTTP client and archive APIs are preferred.

## Release rule

Every plugin release must include player-facing GitHub release notes. The notes
are mandatory because OZ Tools displays them before an administrator can start
an update; empty or placeholder notes are not a valid release.

## Decisions required before implementation

- [x] Use the maintained OZ catalogue for update checks and installation; allow
      compatible public external repositories only when explicitly enabled.
- [x] Default to Devidian/OZ repositories, with an explicit external-repository
      setting for compatible public repositories.
- [x] Support public GitHub releases only.

## Implementation checklist

- [x] Define a strict release-source model from `plugin.yml`, including GitHub
      repository extraction, supported asset selection, timeouts, rate-limit
      handling, and semantic-version comparison.
- [x] Add opt-in settings for automatic checking and its startup delay
      (default: disabled, 30 seconds).
- [x] Start one bounded asynchronous check after startup; provide an
      administrator-only manual check above the overlay close button.
- [x] Surface per-plugin state: green for current, red for update available,
      unchanged for unchecked/unknown.
- [x] Add the administrator-only update action and release notes to the
      selected plugin page, reusing a single confirmation-dialog component.
- [x] Query supported but absent OZ plugins during a manual check and show their
      latest release metadata in the catalogue.
- [x] Persist per-plugin release checks and refresh sidebar state after each
      completed GitHub request; provide a targeted check from release notes.
- [x] Download to a staging directory, validate the archive and expected plugin
      artifact, extract flat into the target plugin directory, remove the
      archive, and request a debounced plugin reload.
- [x] Ensure errors never replace a working package and report actionable
      server-side and UI messages.
- [x] Add canonical `website` release URLs to supported maintained plugins.
- [x] Add focused unit tests for URL parsing, comparison, and release-asset
      selection. HTTP and staging behaviour are covered by the runtime smoke
      test because they depend on the live Rising World plugin environment.
- [x] Update `README.md`, `HISTORY.md`, settings documentation, and i18n.

## Risks and rollback

An update executes downloaded server code, so an administrator confirmation is
required and release sources must be constrained by the selected trust policy.
Installation is staged and validated before the active plugin file is changed;
on failure, the existing file remains in place. Automatic checking is disabled
by default and does not change plugin files. Rollback of a successful update is
not automatic in this phase; an operator restores the previous plugin archive
or deploys a known release.

## Validation

- [x] `mvn -B -Dmaven.repo.local=/tmp/oz-tools-release-m2 test` (13 tests)
- [x] `mvn -B -Dmaven.repo.local=/tmp/oz-tools-release-m2 -DskipTests package`
- [x] Runtime-check manual admin check, current/update state, GitHub rate-limit
      handling, successful update/reload, disabled update action during install,
      immediate version-state refresh after installation, and administrator
      feedback. Confirmed on 2026-07-20 using OZ GPS 0.7.2.
- [deferred] Failed staging with the old plugin retained. This needs a dedicated
      compatible public test plugin and is deferred until external plugin
      repository support is used in practice; it is not an active OZ-plugin
      delivery task.
