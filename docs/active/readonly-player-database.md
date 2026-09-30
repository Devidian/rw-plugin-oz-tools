# Read-only native player queries

Objective: keep `PlayerDatabaseHelper` fallback queries from closing a writable
SQLite connection to the game-owned `Player.db` while its native WAL is active.

Constraints: retain the `WorldDatabase` path and query behavior; change only
the direct SQLite fallback. Admin Utils consumes the shared reader. No release
or Production deployment is part of this Development check.

- [x] Add a shared SQLite connection opened with `mode=ro`.
- [x] Use it for all `PlayerDatabaseHelper` fallback queries.
- [x] Build and run Tools tests; build and run Admin Utils tests against it.
- [x] Verify Development native WAL linkage across repeated query cycles.
- [x] Check a native inventory save and WAL-aware stop/restart during the
  controlled Stargate crash test; bytes remained identical.

Risk: a read-only connection must still see committed WAL frames. The consumer
test covers that, write rejection, and WAL retention. Rollback is the previous
Tools and Admin Utils JAR pair backed up on Development.
