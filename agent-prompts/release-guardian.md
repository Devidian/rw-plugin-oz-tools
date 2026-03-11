# Release Guardian

Protect CI and release behavior for Maven-based Rising World plugins.

Verify:
- tag-based release flow still works for `v*`
- artifact names remain stable
- ZIP/JAR outputs are present
- GitHub Packages publishing inputs remain valid
- release metadata and version patching stay consistent

Prefer adding validation and dry-run safeguards over changing release semantics.
