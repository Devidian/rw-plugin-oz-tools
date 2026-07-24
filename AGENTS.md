# AGENTS.md

## Repository Purpose
This repository owns shared runtime infrastructure for Rising World Unity Java plugins.

It must remain usable standalone. Workspace-root orchestration is optional and must never be required for build, release, or local agent operation.

## Ownership
Owns:
- reusable i18n, logging, UI, settings, WebSocket, watcher, and SQLite helpers
- shared contracts and utilities that are demonstrably reusable by sibling plugins
- compatibility foundations consumed by `rw-plugin-oz-*` feature plugins

Does not own:
- feature-plugin business logic
- land claim, GPS, Discord, intercom, or admin utility domain rules
- workspace-root orchestration rules

## Mandatory Workflow Rules
- Preserve the Java 20 baseline.
- Preserve Maven build and GitHub tag-release behavior.
- Keep dependencies minimal and runtime-safe.
- Reject business logic leakage into this shared library.
- Follow `.codex/agents.toml` for local agent roles, task classes, context loading, and escalation.
- Follow `docs/policies/repository-policy.md` for reusable governance rules.
- Keep `README.md`, `HISTORY.md`, and `PLANS.md` aligned with behavior or structure changes.
- Keep the `plugin.yml` entry class as the sole Rising World `Listener` and sole
  `registerEventListener(...)` target. It may only wire lifecycle, delegate
  events/settings, and expose thin compatibility facades; reusable runtime,
  persistence, UI, integrations, and timers belong in thematic classes.

## Validation
- Run `mvn -B -DskipTests package` for build-impacting changes.
- Run `mvn -B test` when tests exist.
- Verify new Rising World API usage before relying on it.
- Review consuming plugin impact when shared APIs or contracts change.
