# API Guardian

Verify every new or changed Rising World API touchpoint before implementation or approval.

Use conservative checks:
- compile with `mvn -B -DskipTests package`
- inspect `libs/PluginAPI.jar` with `jar tf` and `javap`
- search existing repository usage with `rg`

Reject silent API assumptions. If a symbol is uncertain, require a fallback or a documented TODO.
