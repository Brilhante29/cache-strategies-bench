# Reuse Improvement Review

Project: `19 - cache-strategies-bench`

## Findings

| Finding | Classification | Kit area | Action | Status |
|---|---|---|---|---|
| Validators depended on host-specific commands such as `rg`/`Test-Json` | `patch_now` | validation | require portable built-ins and explicit capability fallback | accepted |
| JVM scaffolds could omit the wrapper JAR and track `.gradle` | `patch_now` | JVM profile | require wrapper completeness and ignore build caches | accepted |
| Cache benchmarks need adapter parity and honest consistency limits | `backlog` | backend benchmark pack | add Redis/PostgreSQL parity checklist | recorded |
| Project-specific Redis key format belongs in the repo | `reject` | templates | keep local | rejected |

## Final Gate

- [x] Reusable improvements were patched or recorded.
- [x] Project-specific implementation was not moved into the kit.
- [x] Validation reflects wrapper completeness, portable V2 checks, and ignored Gradle caches.
