# PLANS.md

General planning framework for the **planner** role across `rw-plugin-oz-*` repositories.

## Purpose
This document defines how planning should be done before implementation, so work stays predictable, auditable, and safe for Rising World Unity plugin development.

## Planner responsibilities
1. Restate the request in implementation terms.
2. Identify constraints (Java 20, API availability, CI/tag release behavior, dependency policy, README/HISTORY maintenance, Conventional Commits).
3. Propose the smallest viable change set.
4. Define validation strategy (build, tests, runtime/log checks).
5. Flag risks, unknowns, and fallback options.

## Planning output format (required)
A planner output should always contain:
- **Scope**: what will and will not be changed.
- **Files**: exact files expected to change.
- **Steps**: ordered execution steps.
- **Validation**: exact commands/checks to run.
- **Risks**: uncertainty points and mitigations.
- **Done criteria**: objective completion checks.

## Standard planning sequence
1. **Context scan**
   - Read relevant files only (README, `pom.xml`, `plugin.yml`, affected source files, AGENTS docs).
2. **Constraint extraction**
   - Record hard constraints and non-goals.
3. **Impact mapping**
   - List changed files and side effects (runtime behavior, config, CI, release artifacts).
4. **Validation design**
   - Include compile/test commands and runtime log inspection path when relevant.
5. **Execution handoff**
   - Provide concise, implementation-ready steps for implementer/test-runner.

## Required checks to include in plans
- Compile check: `mvn -B -DskipTests package`
- Tests (if present): `mvn -B test`
- API verification approach for new API calls (`jar tf` / `javap` / code search)
- Runtime log location when Docker/server checks are performed:
  - `/appdata/rising-world/dedicated-server/Logs`

## Risk model
Classify risks with one of these levels:
- **Low**: local/refactor changes, no API surface or workflow impact.
- **Medium**: touches plugin behavior, settings, or command handling.
- **High**: touches CI/release workflow, dependency model, or uncertain API methods.

For Medium/High risk, planner must define rollback/fallback notes.

## Reuse guidance
- Keep this file generic; project-specific details belong in local `PLAN.md`.
- Use the same planner format across sibling repositories for consistency.

## Documentation and changelog planning requirements
Planner output must explicitly state whether `README.md` and `HISTORY.md` are impacted:
- If behavior/config/commands/install steps change, include README update steps.
- If user-visible changes are introduced, include HISTORY update steps using the release policy (major/minor heading; patch note in current section).
- Include a commit-message plan that follows Conventional Commits.
