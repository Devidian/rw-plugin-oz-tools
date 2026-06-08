# Portfolio Design And Settings Roadmap

## Objective
Provide the shared foundation required by Roadmap Plan 01: a portfolio-wide `DESIGN.md`, reliable `settings.properties` reload behavior, and an admin-only `PluginSettings` tab in the shared player plugin settings overlay.

## Ownership
Primary repository: `rw-plugin-oz-tools`.

Supporting repositories:
- `rw-plugin-maven-template` for future-plugin baseline adoption.
- Existing plugins consume the shared settings/admin-tab APIs.

## Dependencies
- No new runtime dependency should be added to Tools.
- Existing feature plugins already require Tools and should consume the new shared API once released.

## Confirmed Decisions
- `reloadOnChange` should default to `true` in all plugins.
- The admin `PluginSettings` tab should be editable if feasible in the first implementation.
- Sensitive settings must always be hidden from the admin settings UI.
- `DESIGN.md` must exist at root and as synchronized per-repository copies so repository-local workers can operate autonomously.
- Admin settings editor v1 supports simple types only: booleans, integers, and strings.

## Work Packages
- [x] Package 1: Derive a shared root `DESIGN.md` from current Wallet, LandClaim, GPS, and Tools UI patterns.
- [x] Package 1a: Copy/synchronize `DESIGN.md` into each plugin repository and document the rule that root changes must be propagated.
- [x] Package 2: Define shared status-indicator placement rules for LandClaim area info, Shop, Marketplace, and LandClaim sale indicators.
- [x] Package 3: Introduce a reusable settings metadata model for displaying `settings.properties` keys, current values, defaults, and admin-facing descriptions.
- [x] Package 4: Extend `PlayerPluginSettingsOverlay` with an admin-only third tab named `PluginSettings`.
- [x] Package 5: Add a shared reload button contract that calls the owning plugin's settings reload path and refreshes the tab after reload.
- [x] Package 6: Standardize settings-file watcher behavior so all plugins using the mechanism reload `settings.properties` consistently.
- [x] Package 7: Add base UI components for editable settings rows where safe, plus read-only/hidden handling for sensitive or unsupported values.
- [x] Package 8: Document integration steps for existing and future plugins.

## Step 1 Result
- Added the canonical root `DESIGN.md`.
- Added synchronized `DESIGN.md` copies to the existing Rising World plugin repositories and `rw-plugin-maven-template`.
- Captured status-indicator placement, admin-only UI, settings-tab safety rules, table/list/card usage, localization, asset, and synchronization expectations.
- Left implementation packages for settings reload, admin settings metadata, and editable settings UI pending for Roadmap Plan 01 Step 2 and later.

## Step 2 Result
- Added `PlayerPluginAdminSettings` registration for admin-only settings metadata.
- Added an admin-only `PluginSettings` tab to `PlayerPluginSettingsOverlay`; normal players cannot see the tab.
- Added shared rows for simple boolean, integer, and string values, with sensitive entries hidden and unsupported entries read-only.
- Added a reload contract that calls the owning plugin reload action and refreshes displayed values.
- Added `SettingsFileEditor` for simple `settings.properties` value writes while preserving surrounding comments and unrelated keys.
- Registered Tools' own settings metadata as the first consumer and kept existing `FileChangeListener` settings reload behavior as the shared watcher path.

## Risks
- Tools must not absorb feature-plugin settings business rules; it should render metadata and trigger reloads only.
- The admin-only tab must never expose admin settings to normal players.
- Sensitive values must be hidden even from admins unless explicitly marked safe by metadata.
- The file watcher currently registers listener plugins at Tools startup; plugin reload order and late plugin registration need verification.

## Validation Strategy
- Verify Tools compiles and existing settings/data tabs still render.
- Verify admin users see `Settings`, `Data`, and `PluginSettings`; normal users only see allowed tabs.
- Verify changing `settings.properties` reloads settings for each existing plugin that implements `FileChangeListener`.
- Verify the manual reload button refreshes values and does not require full plugin reload.

## Affected Repositories/Plugins
- `rw-plugin-oz-tools`
- `rw-plugin-maven-template`
- all existing Rising World feature plugins during adoption
- future `rw-plugin-oz-shop`
- future `rw-plugin-oz-marketplace`

## Rollback Considerations
Keep the existing two-tab overlay behavior as the fallback. If the admin settings tab causes UI issues, consuming plugins can omit metadata registration while normal settings reload behavior remains intact.

## Open Questions
- None.
