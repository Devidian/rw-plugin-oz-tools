# Roadmap Plan 04 Shared Runtime And UI Standards

## Objective
Provide the shared support needed for Plan 04 portfolio standards, then remove the deprecated `SQLite` class only after all plugins have migrated away from it.

## Ownership
Primary repository: `rw-plugin-oz-tools`

Supporting repositories:
- Every Rising World plugin that consumes Tools persistence, shared UI, i18n, settings, or menu registration.
- `rw-plugin-maven-template` for future-plugin defaults.

## Dependencies
- Existing Tools shared UI, player settings, i18n, and persistence helpers.
- Deprecated `SQLite` removal is blocked by a full consumer audit and migrations to `SQLiteConnectionFactory`.

## Phases
- [x] Phase 1: Audit Tools public APIs and all sibling plugins for deprecated `SQLite` usage.
- [x] Phase 2: Document and, if useful, expose the canonical `SQLiteConnectionFactory` migration path for repository-local agents.
- [x] Phase 3: Add hover tooltips to the shared inventory plugin button panel and increase icon rendering to roughly 1.5x current size without changing registration contracts.
- [x] Phase 4: Provide or document a standard player setting convention for showing/hiding each plugin shortcut in `/ozt open` and the inventory button panel.
- [x] Phase 5: Provide or document a standard escape-close behavior for Tools-hosted overlays and nested plugin panels.
- [x] Phase 6: Verify i18n resource loading happens once per plugin during `onEnable`; fix Tools-side duplicate loading if present.
- [x] Phase 7: Remove the deprecated `SQLite` class only after all consuming plugin migrations are complete and validated.
- [x] Phase 8: Update README/HISTORY and validate Tools plus representative consuming plugins.

## Progress Notes
- Phases 1-8 are complete.
- `PluginShortcutVisibility` provides the shared default-visible visibility convention and setting-key helper.
- Custom-overlay Escape support was removed and deferred to the future Rising World API layer.
- `I18n.getInstance(Plugin)` now loads language resources once per plugin path instead of reloading files on later cached lookups.
- Inventory shortcut icons increased from 24px to 36px. The Rising World UI API has no native custom-element tooltip property, so compact labels are rendered inline instead of hidden hover-only labels.
- Players can disable inventory shortcut labels in the OZTools player settings; the preference is persisted in the Tools player settings database and defaults to visible.
- Fixed the filtered `/ozt` menu item list so the Settings entry can be appended after player-aware visibility filtering.
- `AdminSettingsType.SELECT` now renders editable enum-like settings through the shared dropdown control.
- Deprecated `SQLite` was removed after Shop, GPS, and the remaining portfolio runtime-standard packages validated with no direct consumers.
- Validation passed with `mvn -B test` and `mvn -B install` after adding select support.

## Risks
- Removing deprecated persistence support before consumers migrate will break compilation or runtime startup.
- Tooltip/icon sizing can disrupt compact inventory layout if stable dimensions are not preserved.
- Custom-overlay Escape behavior depends on the future Rising World API layer.

## Validation Strategy
- Run `mvn -B test` and `mvn -B -DskipTests package` in Tools.
- Run compatibility tests in Shop, Marketplace, GPS, Rewards, Wallet, LandClaim, Admin Utils, Discord Connect, and Global Intercom after shared API changes.
- Runtime-smoke inventory shortcut tooltips, larger icons, shortcut visibility, and one-time i18n load logs.

## Affected Repositories/Plugins
- `rw-plugin-oz-tools`
- All Rising World plugins through shared Tools integration.

## Rollback Considerations
Keep new UI/settings support additive. Delay deprecated `SQLite` removal until the final Plan 04 cleanup so consumers can be migrated and released incrementally.
