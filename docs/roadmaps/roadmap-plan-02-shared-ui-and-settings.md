# Roadmap Plan 02 Shared UI And Settings

## Objective
Provide the shared UI and settings foundation for Roadmap Plan 02 so feature plugins can expose consistent inventory access buttons, compact indicators, plugin info/status panels, dynamic tabs, and safer admin settings controls.

## Ownership
Primary repository: `rw-plugin-oz-tools`.

Supporting repositories:
- All Rising World feature plugins consume the shared UI/settings APIs.
- `rw-plugin-maven-template` adopts the new integration baseline after Tools contracts are stable.

## Dependencies
- No new runtime dependency should be added to Tools.
- Existing plugin menu, player settings, overlay, i18n, and asset helpers are the preferred integration points.

## Work Packages
- [x] Package 1: Add a transparent inventory-overlay helper panel below the inventory that lists registered plugin buttons in flex layout sorted by plugin name.
- [x] Package 2: Add a transparent shared indicator helper panel where plugins can register indicator providers with `showIndicator(Player)` and `getIcon(Player)` callbacks.
- [x] Package 3: Add a centered square plugin info/status helper panel with title `Plugin: PH_PLUGIN_NAME`, close button, and `Info` / `Status` tabs.
- [x] Package 4: Define the plugin info/status provider contract with plugin-specific `getInfo(Player)` and `getStatus(Player)` content providers.
- [x] Package 5: Sort `/ozt` main radial menu plugin entries deterministically by plugin name.
- [x] Package 6: Refactor tab overlays so tabs can be dynamically added into an invisible flex container, allowing feature plugins to hide unavailable tabs.
- [x] Package 7: Extend shared settings UI so numeric inputs reject non-numeric characters.
- [x] Package 8: Add shared support for labeled settings separators/groups, such as `General Settings` or plugin-specific setting groups.
- [x] Package 9: Document integration rules for plugin button registration, indicator registration, dynamic tabs, and info/status providers.
- [x] Package 10: Apply the one-main-logger rule to OZ.Tools itself.
- [x] Package 11: Complete OZ.Tools admin `PluginSettings` metadata, grouped settings labels, numeric input behavior, and English/German i18n labels.
- [x] Package 12: Add OZ.Tools info/status content for the shared plugin info/status panel.

## Risks
- Tools must provide generic UI contracts only; GPS, Shop, Marketplace, and other business rules stay in their owning plugins.
- Inventory overlay placement must not cover core inventory controls or existing plugin overlays.
- Shared indicator ordering and sizing must stay deterministic when multiple plugins register icons.
- Dynamic tab changes can affect existing overlays if default behavior is not backward compatible.

## Validation Strategy
- Verify Tools compiles and existing menu/settings overlays still render.
- Verify `/ozt` ordering remains stable across repeated menu opens.
- Verify registered plugin buttons and indicators sort consistently.
- Verify a plugin can hide and show dynamic tabs without layout gaps or stale event handlers.
- Verify integer settings fields reject non-numeric input before plugin-specific adoption.

## Affected Repositories/Plugins
- `rw-plugin-oz-tools`
- all Rising World feature plugins during adoption
- `rw-plugin-maven-template`

## Rollback Considerations
Keep existing radial menu and overlay components available while plugins migrate to the new registration APIs.

## Open Questions
- None for routing.
