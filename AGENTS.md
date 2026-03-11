# AGENTS.md

Guidelines for agent-driven work in `rw-plugin-oz-*` projects (Rising World Unity Java plugins).

## 1) Project context (mandatory)
- Project type: Maven-based Java plugin for **Rising World (Unity)**.
- Java version: **20** (must always match the game runtime requirement).
- Release process: GitHub Actions builds artifacts; **releases are created from Git tags** (`v*`).
- Sibling repository naming pattern: `rw-plugin-oz-*`.
- Base template repository: `rw-plugin-maven-template`.
- Architecture principle: keep feature plugins slim; prefer shared external libraries in `rw-plugin-oz-tools`.

## 2) Working rules for CODEX agents
1. **API safety before implementation**
   - Before using new Rising World API methods, verify they actually exist.
   - Preferred checks:
     - Compile with Maven (`mvn -B -DskipTests package`)
     - Inspect signatures in local `PluginAPI.jar` (`jar tf`, `javap`)
     - Search existing usage in repository (`rg "MethodName|ClassName" src`).
2. **No silent API assumptions**
   - If a method is uncertain, use a conservative alternative or add a TODO with clear rationale.
3. **Java 20 remains the baseline**
   - Never accept changes that lower source/target/release below 20.
4. **Keep dependencies minimal**
   - Add new external libraries only when technically necessary and compatible.
   - Evaluate whether functionality belongs in `rw-plugin-oz-tools` instead.
5. **Preserve release/CI compatibility**
   - Tag-based release workflows must not be broken.
   - Build artifacts and expected file names must remain consistent.

## 3) Standard workflow per task
1. Understand requirements and identify affected files.
2. Validate API availability (PluginAPI/codebase/compile checks).
3. Implement with small, traceable commits.
4. Run local checks:
   - `mvn -B -DskipTests package`
   - `mvn -B test` when tests exist.
5. Document outcome (change summary, risks, open points).

## 4) Recommended role model
- **planner**: clarifies scope, creates implementation plan, identifies risk.
- **api-guardian**: validates Rising World API usage + Java 20 compliance.
- **implementer**: delivers minimal, maintainable changes.
- **test-runner**: runs build/tests and optional runtime checks in RW Docker.
- **release-guardian**: validates CI/tag-release behavior, versioning, artifacts.
- **reviewer**: final quality pass (readability, robustness, backward compatibility).

## 5) Docker runtime test context (optional, recommended)
Recommended image: `devidian/rising-world-docker:latest`
- Game root in container: `/appdata/rising-world/dedicated-server`
- Plugin deployment path: `/appdata/rising-world/dedicated-server/Plugins/<PluginName>/`
- Log path for runtime troubleshooting: `/appdata/rising-world/dedicated-server/Logs`
- Goal: validate built plugin files against a realistic running server setup.

## 6) Definition of Done
- Build succeeds with Java 20.
- New/changed API calls are verified.
- CI/release mechanics remain intact.
- Changes are documented and reproducible.

## 7) Repository documentation and commit policy
- Keep repository-facing governance/configuration documents in the **repository root** by default (`AGENTS.md`, `PLANS.md`, `config.toml`), unless tooling explicitly requires a different location.
- `.codex/` or `.agent/` folders are optional, but not required for this project baseline.
- `PLAN.md` should be treated as a local, one-off planning artifact and should normally **not** be committed unless explicitly requested.
- Keep `README.md` and `HISTORY.md` maintained for every meaningful change:
  - `README.md` must describe plugin purpose, installation, and usage.
  - `HISTORY.md` must document version-to-version changes.
  - For **major/minor** releases: add a new version heading.
  - For **patch** releases: add a concise note under the relevant current version section.
- Commit messages must follow **Conventional Commits**: <https://www.conventionalcommits.org/en/v1.0.0/>.
