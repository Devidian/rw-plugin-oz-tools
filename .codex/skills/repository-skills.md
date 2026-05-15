# Repository Skills

## rw-api-validation
- Verify new Rising World API usage before implementation.
- Prefer `mvn -B -DskipTests package`, `jar tf libs/PluginAPI.jar`, `javap`, and `rg` over assumptions.
- If an API remains uncertain, use a conservative fallback or document the uncertainty in the active task.

## runtime-debugging
- Use local build output first, then optional Rising World Docker/server validation.
- Check plugin deployment path and server logs when runtime behavior is involved.
- Record log paths and exact reproduction steps in the active task.

## sqlite-migration-review
- Treat SQLite schema or data changes as migrations.
- Require rollback notes, backward compatibility review, and validation against existing data.
- Keep persistence helpers reusable in `rw-plugin-oz-tools` unless the logic is feature-specific.

## websocket-contract-review
- Treat WebSocket payload changes as public contracts.
- Verify producer and consumer compatibility.
- Document versioning, fallback, and failure behavior.

## release-validation
- Preserve Java 20, Maven packaging, artifact names, and GitHub tag-release behavior.
- Check `HISTORY.md` and release workflow changes together.
- Never require workspace-root files for a repository release.
