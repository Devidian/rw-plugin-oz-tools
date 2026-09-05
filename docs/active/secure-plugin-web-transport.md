# Secure Plugin Web Transport

## Objective

Provide the reusable Tools-owned credential guard and outbound WebSocket transport required by plugin-owned native web routes, without moving feature business logic into Tools.

## Ownership

Tools owns the private credential store, pairing handler, `Authorization` validation facade, event registry and non-blocking WebSocket client. Tools must remain usable standalone and expose no Manager-specific feature semantics.

## Dependencies

The work depends on the protocol agreed in the root coordination plan and on the existing runtime-safe `nv-websocket-client`. Consumers are Admin Utils first, then GPS, Marketplace, Shop and Land Claim route handlers.

## Checklist

- [ ] Inspect existing settings, private persistence, native handler and lifecycle APIs; retain the entry-class listener-only architecture.
- [x] Add defaulted `web.allowUnsecureRequests=false`, `web.useWebsockets=false` and the documented WSS target through the existing settings/i18n model; exclude credentials and pairing codes from PluginSettings UI, exports and reload diagnostics.
- [x] Implement durable private credential storage, with atomic installation only after the backend's authenticated provisioning response. Tools creates its private random at-rest key automatically on first start; `OZ_TOOLS_GAME_CONNECTOR_CREDENTIAL_KEY` remains an optional 32+ character deployment override. Existing environment-key credentials are not silently re-keyed: retain that variable until a deliberate credential reset and re-pair.
- [x] Start a lifecycle-owned outbound provisioning WSS connection while no credential exists, independent of `web.useWebsockets`; accept credentials only from the configured TLS-authenticated backend and reject a replacement when already paired.
- [~] Add a reusable handler guard that validates the agreed Authorization scheme before plugin DTO work and produces uniform `401` responses; focused header contract tests remain.
- [ ] Provide typed, bounded publish/subscribe registration and live feature registry APIs; declared-feature registration is bounded and live, while payload publish/subscribe remains for the event-delivery milestone.
- [ ] Implement lifecycle-owned WSS connection, auth, heartbeat, bounded reconnect/backoff and feature renegotiation. Connection/reload/shutdown handling is implemented; heartbeat, bounded backoff and feature registry remain.
- [~] Document the public shared API and configuration; release migration and local credential-reset procedure remain.

## Risks, Rollback and Validation

The backend binds automatic pairing to the actual peer address, not a public HTTP first-writer route. Never store a bearer credential in exposed settings, routes or logs. `wsTargetUrl` authenticates the backend through TLS but does not identify the game server. Maintain `allowUnsecureRequests` solely as an explicit staged rollback; its secure default must not change. Consumers must tolerate Tools missing or an older artifact by retaining their current route behavior until the coordinated release wave installs the new API.

## Validation

Add focused tests for defaults, private-state redaction, first provisioning, duplicate provisioning rejection, reset, valid/invalid/missing headers, no-op event publishing, reconnection and feature rebuild. Run `mvn -B test` and `mvn -B -DskipTests package`. Build the installed artifact before consumer validation; Development runtime proof waits for reload and startup/listener evidence.

## Affected Repositories/Plugins

- `rw-plugin-oz-tools`
- `rw-plugin-oz-admin-utils`
- `rw-plugin-oz-gps`
- `rw-plugin-oz-marketplace`
- `rw-plugin-oz-shop`
- `rw-plugin-oz-land-claim`

## Rollback Considerations

Revert consumers before removing the shared API. Keep the explicit insecure setting only as a short, audited staging escape hatch; it never clears private credentials or replaces the administrator reset flow.
