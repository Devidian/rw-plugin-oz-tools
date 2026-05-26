# Roadmap Plan 03 Shared UI And Wallet Bridge Support

## Objective
Polish shared UI behavior needed by Plan 03, centrally register the new shared icon assets, and support the portfolio-wide menu conventions without moving feature-plugin business logic into Tools.

## Ownership
Primary repository: `rw-plugin-oz-tools`

Supporting repositories:
- `rw-plugin-maven-template` for future-plugin adoption.
- Consuming plugins for feature-specific menu entries, selectors, and indicators.

## Dependencies
- Tools remains the hard runtime dependency for every Rising World plugin.
- WalletBridge business/API ownership remains with Wallet and consuming plugins, not Tools.
- The three new shared icons are registered by Tools. Consuming plugins reference them by key and do not register duplicate copies.

## Phases
- [x] Phase 1: Change the inventory plugin panel to show icons only, preserving stable button dimensions and click behavior.
- [x] Phase 2: Register the three new shared icon assets in Tools: one shared Info/Status icon for all plugin radial menus and two generic placeholder icons for future features without dedicated icons.
- [x] Phase 3: Register Tools itself as an inventory-panel icon and add/open Tools' Info/Status panel from that entry.
- [x] Phase 4: Fix the inventory-panel registration issue where only the Maven/template plugin button is visible.
- [x] Phase 5: Hide shared zone indicators while the inventory UI is open, matching LandClaim zone-info behavior.
- [x] Phase 6: Provide only generic UI support for dropdown/selectbox or icon-grid needs if existing Tools controls are insufficient; keep Marketplace and Shop behavior in their repositories.
- [x] Phase 7: Document integration rules for icon-only inventory buttons, shared icon keys, indicator visibility, and radial Info/Status buttons.

## Risks
- Inventory-panel changes can affect every plugin that registers a button.
- Shared icon key changes can break every radial Info/Status button if plugins hardcode different names.
- Indicator hiding can regress Shop and Marketplace zone feedback if refresh timing is wrong.
- A shared selector helper should remain generic; currency-specific behavior belongs in Wallet/Marketplace.

## Validation Strategy
- Run `mvn -B -DskipTests package`.
- Run `mvn -B test`.
- Runtime-smoke at least Tools, Wallet, Shop, Marketplace, and GPS with multiple registered inventory buttons.
- Verify the shared Info/Status icon resolves from every plugin radial menu that uses it.
- Verify indicators appear outside inventory and hide while inventory is open.

## Affected Repositories/Plugins
- `rw-plugin-oz-tools`
- All Rising World plugins consuming Tools shared UI

## Rollback Considerations
Keep registration APIs intact. If icon-only layout causes runtime issues, restore labels visually without changing consumer registrations.

## Progress Notes
- Phase 1 complete: `InventoryOverlayPanel` now renders stable icon-only 38 px buttons. Labels remain registration metadata and sort keys.
- Phase 2 complete: Tools now loads `icon-ki-info-status`, `icon-ki-placeholder`, and `icon-ki-soon` through `AssetManager.loadDefaultIcons`.
- Phase 3 complete: Tools registers its own inventory button and opens the shared Tools Info/Status panel from that entry.
- Phase 4 complete: inventory panels refresh when button registrations change, so already-open inventories can pick up late registrations.
- Phase 5 complete: shared indicators are removed while inventory is open and refreshed when inventory closes.
- Phase 6 complete for this package: no new shared selector control was required before Marketplace/Shop implementation; feature-specific dropdown and icon-grid behavior remains in those plugins unless later implementation proves a generic helper is needed.
- Phase 7 complete: README/HISTORY now document shared icon keys, icon-only inventory buttons, and indicator visibility behavior.
- Validation passed with `mvn -B test` and `mvn -B install` in Tools. After local Tools installation, `mvn -B test` passed in Wallet, Shop, Marketplace, and GPS.
