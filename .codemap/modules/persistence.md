# SQLite persistence interfaces, caching and player settings
<!-- codemap-id: rw-plugin-oz-tools/persistence -->

## Responsibility

SQLite persistence interfaces, caching and player settings. Tools supplies reusable persistence primitives and owns shared player settings. Feature plugins own their domain databases.

## Source and tests

- [`SQLiteConnectionFactory`](../../src/de/omegazirkel/risingworld/tools/db/SQLiteConnectionFactory.java)
- [`SQLiteCachedStore`](../../src/de/omegazirkel/risingworld/tools/db/SQLiteCachedStore.java)
- [`DatabaseSchema`](../../src/de/omegazirkel/risingworld/tools/db/interfaces/DatabaseSchema.java)
- [`PlayerSettings`](../../src/de/omegazirkel/risingworld/tools/PlayerSettings.java)
- [`PlayerSettingsErasureTest`](../../src-test/de/omegazirkel/risingworld/tools/PlayerSettingsErasureTest.java)

See the [repository index](../README.md) for lifecycle, build and cross-repository dependencies.
