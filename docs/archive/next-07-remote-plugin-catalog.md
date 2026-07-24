# Next 07: Remote trusted plugin catalogue

## Objective

Move the trusted OZ plugin list out of Java code and refresh it from the OZ
Tools repository before release checks. Adding or revoking a trusted plugin
must not require a new Tools release. Include OZ Bosses as an installable
catalogue entry.

## Ownership and dependencies

OZ Tools owns catalogue retrieval, validation, fallback, trust enforcement, and
the plugin-manager integration. Each listed feature plugin continues to own its
release metadata and release archive.

Affected repositories/plugins:

- `rw-plugin-oz-tools`: repository catalogue, bounded remote loading, trust
  enforcement, tests, and documentation.
- `rw-plugin-oz-bosses`: canonical public GitHub release URL in `plugin.yml`.

No new dependency is required. The existing Java HTTP client and Gson parser
are used.

## Checklist

- [x] Add a versioned JSON catalogue in the Tools repository.
- [x] Include OZ Bosses with its canonical repository and install directory.
- [x] Package the same catalogue as the startup/offline fallback.
- [x] Refresh the catalogue through a fixed HTTPS repository URL before update
      checks.
- [x] Bound and strictly validate catalogue size, schema, repositories,
      directories, and duplicates.
- [x] Use exact catalogue membership as the default trust decision.
- [x] Preserve explicitly enabled external public repository support.
- [x] Add focused catalogue parsing and Bosses-presence tests.
- [x] Update Tools and Bosses documentation and metadata.
- [x] Run Tools tests/package and Bosses package validation.

## Risks and rollback

The remote catalogue controls which release repositories administrators may
install from, so its URL is fixed to the OZ Tools repository and entries are
limited to canonical `Devidian/rw-plugin-oz-*` repositories and safe single
directory names. Invalid or unavailable remote content never replaces the last
valid catalogue; the packaged catalogue remains the startup fallback.

Rollback is a revert of the service and catalogue changes. Removing an entry
from the remote catalogue revokes its default trust for updated Tools
installations without replacing their local plugin files.

## Validation

- `mvn -B test`
- `mvn -B -DskipTests package`
- Confirm `OZ - Bosses` appears as not installed after a manual update check
  and resolves a ZIP asset from its latest public GitHub release.

Validated on 2026-07-24 from writable temporary copies:

- OZ Tools: 21 tests passed and `OZTools-0.23.9.zip` was packaged; the JAR
  contains the bundled `plugin-catalog.json`.
- OZ Bosses: `OZBosses-0.1.0.zip` was packaged with the canonical release URL
  in its JAR metadata.
- GitHub release `v0.1.0` exposes `OZBosses-0.1.0.zip`.
