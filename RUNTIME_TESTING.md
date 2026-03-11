# Runtime Testing

Standard runtime smoke-test flow for `rw-plugin-oz-*` repositories.

## Prerequisites

- Docker image: `devidian/rising-world-docker:latest`
- Server root inside the container: `/appdata/rising-world/dedicated-server`
- Plugin deployment directory: `/appdata/rising-world/dedicated-server/Plugins`
- Log directory: `/appdata/rising-world/dedicated-server/Logs`

## Local workflow

1. Build the plugin:
   - `mvn -B -DskipTests package`
2. Copy the built plugin folder from `dist/<PluginName>/` into the server plugin directory.
3. Start or restart the Rising World server.
4. Inspect the latest files in `/appdata/rising-world/dedicated-server/Logs`.

## Minimum smoke-test expectations

- The plugin loads without startup exceptions.
- The plugin folder contents are complete after deployment.
- Plugin commands or the main advertised feature can be exercised once.
- No new severe warnings or stack traces appear in startup logs.

## Helper script

Use the repository helper when the server filesystem is locally reachable:

```bash
scripts/docker-runtime-smoke.sh <PluginFolderName>
```

The script deploys `dist/<PluginFolderName>/` into the configured plugin path and prints the newest log files for inspection.
