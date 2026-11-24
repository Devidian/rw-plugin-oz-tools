---
name: codemap
description: Navigate CodeMap indexes for unfamiliar modules, shared interfaces and cross-repository changes, and maintain the affected entries when indexed architecture changes.
---

# CodeMap

Start with the current checkout's `.codemap/README.md`. For cross-repository work, use the ecosystem index when it is available. For a narrow question, search the index for the relevant topic or link. Read the full module or contract page when its boundaries or invariants matter, then follow its implementation and test links. Use direct source search when you already know the location or the map is missing.

For impact analysis, follow the outgoing dependency IDs to their owning contracts and look for incoming edges. From the ecosystem checkout run `python3 scripts/validate-codemap.py --consumers <repository-or-module-id>`. Check the actual call sites, optional runtime checks, event or wire definitions, and data owner before deciding compatibility.

Source is authoritative. If an entry is stale, continue from verified source and fix that page plus affected references. Update the map as part of the change when an indexed symbol, module, source or test path, public interface, event, dependency, data owner, or invariant changes. Implementation-only changes need no map edit.

Run the owning repository's usual checks. From the ecosystem checkout run `python3 scripts/validate-codemap.py --repo <repository-id>`. Add `--base HEAD` for local Git impact hints. In a standalone checkout, verify the relevant links and symbols directly if the ecosystem validator is unavailable. The ecosystem's `.codemap/standard.md` describes the page format and validator options.
