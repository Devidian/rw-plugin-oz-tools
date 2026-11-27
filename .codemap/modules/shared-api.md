# Shared runtime API
<!-- codemap-id: rw-plugin-oz-tools/shared-api -->

## Responsibility

Shared runtime facade, logging, i18n, GameConnector and optional plugin bridges. Sibling plugins consume this API. GameConnector registration and publication are exposed through OZTools. Verify event producers, backend consumers and consent before changing contracts.

## Source and tests

- [Plugin update actions](../../src/de/omegazirkel/risingworld/tools/ui/PluginUpdateActions.java) check permissions for player-scoped updates and dispatch player callbacks. [Area permission templates](../../src/de/omegazirkel/risingworld/tools/utils/AreaPermissionTemplates.java) copy feature-owned packaged area permissions.

- [`OZTools`](../../src/de/omegazirkel/risingworld/OZTools.java)
- [`OZToolsRuntime`](../../src/de/omegazirkel/risingworld/OZToolsRuntime.java)
- [`GameConnectorService`](../../src/de/omegazirkel/risingworld/tools/GameConnectorService.java)
- [Optional bridges](../../src/de/omegazirkel/risingworld/tools/bridge/) wrap discovery and calls for Wallet, Mail and Factions; the target plugins own their contracts.
- [`OZLogger`](../../src/de/omegazirkel/risingworld/tools/OZLogger.java)
- [`I18n`](../../src/de/omegazirkel/risingworld/tools/I18n.java)
- [`GameConnectorServiceTest`](../../src-test/de/omegazirkel/risingworld/tools/GameConnectorServiceTest.java)

See the [repository index](../README.md) for lifecycle, build and cross-repository dependencies.
