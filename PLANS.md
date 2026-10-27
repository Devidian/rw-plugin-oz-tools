# PLANS.md

- [x] Apply the Tools log level to all OZLogger output paths, including console fallback.
- [x] Validate the logging fix on Development with `logInternal=true` and `logLevel=WARN`; restore the original settings afterward.

- [ ] Validate the next-300926 change on Development with a controlled player check.
- [ ] Validate quick-menu button sizes and plugin navigation width with a player on Development.

Planning is stored in repository-local docs.

- Active implementation tasks: [docs/active/](docs/active/)
- Development validation of read-only native player queries:
  [docs/active/readonly-player-database.md](docs/active/readonly-player-database.md).
- Roadmaps and larger plans: [docs/roadmaps/](docs/roadmaps/)
- Completed phase summaries: [docs/phase-archive.md](docs/phase-archive.md)
- Planning and documentation standards: [docs/policies/repository-policy.md](docs/policies/repository-policy.md)

Every implementation plan must include objective, ownership, dependencies, risks, validation strategy, affected repositories/plugins, rollback considerations, and a markdown checkbox checklist.

Discord event thread routing is implemented locally; Development Discord validation and the separately authorized release remain open. Configuration is documented in README.md.
