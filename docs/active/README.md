# Active Tasks

Active implementation tasks live here.

Use one markdown file per task. Each task must include objective, ownership, dependencies, risks, validation strategy, affected repositories/plugins, rollback considerations, and a markdown checkbox checklist.

## Template

```md
# <Task Name>

## Objective
<What changes and why.>

## Ownership
Owning repository/plugin: `<repo>`
Supporting repositories/plugins: `<repo or none>`

## Dependencies
- Runtime:
- Build:
- Optional integrations:

## Risks
- <risk and mitigation>

## Validation Strategy
- [ ] `mvn -B -DskipTests package`
- [ ] `mvn -B test` when tests exist
- [ ] Runtime/API verification where applicable

## Affected Repositories/Plugins
- `<repo>`

## Rollback Considerations
<How to revert config, migration, behavior, or release changes.>

## Implementation Checklist
- [ ] Add API abstraction
- [ ] Add migration handling
- [ ] Add runtime validation
- [ ] Update README
- [ ] Update HISTORY
```
