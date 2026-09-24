# Next 130926c: GitHub update-check rate limits

## Objective

Make plugin release checks respect GitHub's reported rate-limit reset, block
further GitHub API requests while blocked, optionally authenticate with an
administrator-configured token, and retain known release notes on a rate-limit
response.

## Ownership, dependencies, and compatibility

`rw-plugin-oz-tools` owns the GitHub HTTP client, persisted update metadata,
settings, and administrator feedback. No feature-plugin changes or dependencies
are needed. `githubToken` is optional, defaults to empty, is never surfaced in
the settings overlay, and is sent only as `Authorization: Bearer <token>` to
GitHub API requests.

## Checklist

- [x] Track `X-RateLimit-Remaining` and `X-RateLimit-Reset` from GitHub API responses.
- [x] Prevent later API requests until the reported reset time and show admins a localized message.
- [x] Preserve persisted release metadata when a request is rate limited.
- [x] Add the optional JSON-only token setting and focused unit coverage.
- [x] Build, test, and deploy only OZ Tools to `rw-server-dev`; inspect reload evidence (`RELOADED ALL PLUGINS` at 10:25:22 and the OZ Tools timer callback at 10:25:23 on 2026-09-13).

## Risks, rollback, and validation

The token remains local persistent configuration and is not logged or exposed
in UI. A malformed or unavailable rate-limit header only affects the current
response; it does not create a permanent block. Removing `githubToken` restores
unauthenticated requests. Validate with Maven tests/package and Development
reload logs; no production system is changed.
