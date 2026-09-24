# Next 06: Advanced button refactoring

## Objective

Replace OZ Tools' mutable legacy button styling with state-driven advanced
buttons whose visual and clickable state changes are applied to a stable inner
button inside an invisible layout container.

## Ownership and affected plugins

- Owner: `rw-plugin-oz-tools`
- Consumers: all runtime plugins can adopt the new public UI API; this task
  migrates OZ Tools itself only.
- No template change: the feature is a shared runtime UI API, not scaffolding.

## Dependencies and compatibility

- Uses only the existing Rising World UI API and Java 20.
- Legacy button classes remain source/binary compatible for consuming plugins;
  new OZ Tools code uses `AdvancedButtonFactory`.

## Risks and rollback

- State changes can expose Unity UI hover/click regressions; validate manually
  on the development server before release.
- Rollback is limited to reverting the OZ Tools commit and redeploying the
  previous development artifact.

## Validation

- Maven package and tests.
- Deploy only OZ Tools using `dev-upload.sh`.
- Manual dev-server UI acceptance: default, disabled, and custom state changes
  including hover transitions.

## Checklist

- [x] Add advanced state model, stable inner-button container, and factory.
- [x] Migrate OZ Tools legacy and hand-built buttons to advanced buttons.
- [x] Keep legacy public API compatible and update user-visible history/docs.
- [x] Build OZ Tools; deploy only OZ Tools to the development server.
- [x] Complete manual UI acceptance; release OZ Tools 0.23.0 before migrating consumers.
