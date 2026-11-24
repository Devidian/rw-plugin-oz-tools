# rw-plugin-oz-tools
<!-- codemap-id: rw-plugin-oz-tools -->

## Purpose

Shared infrastructure and compatibility APIs for Rising World plugins. The [plugin descriptor](../src/resources/plugin.yml) names [OZTools](../src/de/omegazirkel/risingworld/OZTools.java) as its lifecycle entry.

## Modules

- [shared-api](modules/shared-api.md) — Shared runtime facade, logging, i18n, GameConnector and optional plugin bridges.
- [persistence](modules/persistence.md) — SQLite persistence interfaces, caching and player settings.
- [ui-settings](modules/ui-settings.md) — Reusable overlays, player settings and privacy UI.

## Build and tests

[Maven](../pom.xml) defines the Java build. Follow [repository validation](../AGENTS.md) for packaging, API checks and runtime validation. Relevant automated tests are under [src-test](../src-test/).
