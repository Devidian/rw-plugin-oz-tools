# Repository Policy

## Runtime Policy
- Java 20 is the baseline for Rising World Unity plugin repositories.
- Do not lower Maven compiler source, target, or release settings below 20.
- Runtime/tooling changes must be reflected in `README.md`, `HISTORY.md`, CI, and packaging files when affected.

## Plugin Entry-Point Policy
- The `plugin.yml` main class is the sole Rising World `Listener` and sole
  `registerEventListener(...)` target.
- Keep it limited to lifecycle wiring, settings/event delegation, and thin
  compatibility facades. Put runtime infrastructure, persistence, UI,
  integrations, and timers in focused thematic classes that do not implement
  Rising World's `Listener`.

## Dependency Policy
- Keep dependencies minimal.
- Add external libraries only when technically necessary and compatible with the plugin runtime.
- Shared runtime helpers, UI helpers, i18n, persistence helpers, WebSocket helpers, logging, and common settings integration belong in `rw-plugin-oz-tools`.
- Feature-specific business logic must stay in the owning feature plugin.

## Shared Library Policy
- `rw-plugin-oz-tools` is a shared foundation, not a feature dumping ground.
- Move logic to `rw-plugin-oz-tools` only when at least one current or likely sibling plugin benefits from the abstraction.
- Do not introduce dependencies from `rw-plugin-oz-tools` back into feature plugins.

## API Verification Policy
- Verify new Rising World API usage before relying on it.
- Preferred checks are Maven compile, `jar tf`, `javap`, and searching existing source usage with `rg`.
- No agent may silently assume uncertain PluginAPI methods.

## Release Policy
- Preserve Maven workflows and GitHub tag-release behavior.
- Release tags use the existing repository convention, normally `v*`.
- Every commit changing `src/`, `pom.xml`, or the bundled PluginAPI must add exactly one descriptive Markdown fragment under `changelog/unreleased/`.
- Before a release, review all pending fragments together: remove reverted or mutually cancelling work, consolidate the remaining entries into `HISTORY.md`, and put only player-relevant highlights in `release-notes/<version>.md`. Remove consumed fragments in the same release-preparation commit.
- Do not publish a release while pending fragments remain.
- Installation or configuration changes require `README.md` updates.

## Documentation Policy
- `PLANS.md` stays intentionally minimal and links to `docs/active/`, `docs/roadmaps/`, and `docs/phase-archive.md`.
- Active tasks belong in `docs/active/`.
- Large plans and roadmaps belong in `docs/roadmaps/`.
- Completed work is summarized in `docs/phase-archive.md`.
- Planning documents must include objective, ownership, dependencies, risks, validation strategy, affected repositories/plugins, rollback considerations, and checkbox progress.
