# Rising World Plugin Portfolio Design Guide

This guide is the shared UI and interaction baseline for the `rw-plugin-oz-*`
Rising World plugins. The root copy is the source of truth. Each plugin
repository keeps a synchronized `DESIGN.md` copy so repository-local agents can
work without relying on root-only orchestration context.

## Core Principles

- Build plugin UIs as in-game tools, not web pages: dense enough for repeated
  admin/player use, with clear actions and minimal explanatory text.
- Prefer existing shared Tools UI classes and patterns before adding plugin-local
  components.
- Keep feature plugins visually consistent: dark translucent panels, restrained
  gold borders, compact tabs, icon-led menu actions, and localized text.
- Do not put feature business logic into `rw-plugin-oz-tools`; Tools owns shared
  rendering conventions and reusable primitives only.

## Visual Language

- Main overlays use a full-screen transparent dark back panel with one centered
  main panel.
- Main panels use near-black translucent backgrounds and gold borders:
  `0.08-0.14` dark RGB panels with `0.55-0.86` alpha, gold border accents near
  `0.95, 0.75, 0.25`, and one-pixel borders unless an existing component already
  requires another width.
- Text hierarchy:
  - titles use bold, roughly 22-26px
  - section descriptions use 12-14px
  - row/card body text uses 12-14px
  - button labels use 13-14px
- Text must fit inside its element. Use wrapping, fixed row heights, or narrower
  copy instead of allowing overlap.
- Avoid new one-off color themes. Plugin-specific colors may signal domain state,
  but the surrounding frame should still match the shared dark/gold style.

## Layout Patterns

- Standard full overlay panels should follow the Wallet/LandClaim shape: centered
  panel, about 78% width, 560px height, title at top left, tabs below the title,
  content body beginning around 120px, and a compact close button at top right.
- Player plugin settings use the shared `PlayerPluginSettingsOverlay`: left
  plugin navigation, top tab bar, and scrollable content.
- Repeated structured data uses shared table components when comparison matters.
  Use list/card layouts for offer listings, marker grids, wallet balances, shop
  offers, and marketplace items.
- Scroll bodies need explicit heights or stable percent constraints so rows,
  cards, and tab changes do not shift or overflow.
- Do not nest decorative cards inside cards. A panel may contain rows, cards,
  lists, tables, or dialogs, but avoid stacked framed containers unless the
  nested frame has a clear functional purpose.

## Controls

- Use icon-first actions for radial menus, close/back/navigation, edit/delete,
  buy/sell, visibility, and marker/zone commands.
- Use text buttons for confirmation actions where the consequence needs a clear
  label, such as buy, cancel, save, close, pardon, or withdraw.
- Destructive actions should use danger styling or an explicit confirmation
  dialog.
- Toggle-like settings use paired on/off controls or a shared switch pattern.
- Tabs are compact rectangular controls with active gold text/border state and
  dark inactive state.
- Admin-only controls must be hidden from non-admin players, not merely disabled.

## Area Status Indicators

- LandClaim remains the anchor for area context. Extra state indicators from
  Shop, Marketplace, and claim sale features should appear below the LandClaim
  area information, stacked vertically with consistent icon sizing and spacing.
- Indicators must not cover inventory, radial menus, plugin overlays, or the
  existing claim text.
- Multiple indicators may be visible at the same time. Use deterministic order:
  Shop, Marketplace, claim sale, then plugin-specific temporary states.
- Indicator icons should use plugin assets loaded through `AssetManager`; avoid
  text-only badges for persistent area states.

## Tables, Lists, And Cards

- Tables are for admin data, permissions, transaction history, top balances, and
  settings metadata. Keep headers short and column widths explicit.
- Lists/cards are for player offers, marker destinations, currencies, and shop
  entries. Each card should have one primary action and only compact secondary
  icon actions.
- Empty states should be short and localized.
- Loading or error states should stay inside the same content body, not spawn a
  separate visual style.

## Plugin Settings UI

- The admin `PluginSettings` tab planned for Tools must be admin-only.
- Sensitive values must be hidden even from admins unless a plugin explicitly
  marks them as safe to display.
- Editable settings v1 is limited to booleans, integers, and strings.
- Unsupported or sensitive settings should render as read-only or hidden based on
  metadata.
- Settings reload actions should call the owning plugin reload path and refresh
  the visible values after reload.

## Localization And Assets

- All player-visible text belongs in plugin i18n files unless it is temporary
  debug-only output.
- Prefer existing icon assets. New icons should follow the current flat,
  readable, high-contrast style and be registered through `AssetManager`.
- Asset icon keys should be stable and plugin-prefixed where a collision is
  plausible.

## Synchronization Rule

- Update the root `DESIGN.md` first.
- Copy the same content into every Rising World plugin repository that will be
  touched by the roadmap before implementation starts there.
- If a repository-local copy intentionally diverges, document the reason in that
  repository's roadmap or active task notes.
