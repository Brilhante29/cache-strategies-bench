# Agent Handoff

Project: `19 - cache-strategies-bench`

## Current State

- Branch: `codex/backend-reliability-close`.
- Architecture: hexagonal; domain strategies depend on cache/repository ports.
- Runtime: Java 21 + Redis 7 + PostgreSQL 16 through Docker Compose.
- Evidence: `benchmarks/results/cache-strategies-v2.json`, three real repetitions.
- Primary number: write-through median p95 `4.517591 ms`; cache-aside `4.761289 ms`.

## Continue Safely

1. Run `docker build -t cache-strategies-bench:dev .`.
2. Run the Compose smoke path documented in README.
3. Run `tools/validate-project.ps1 -SkipDocker` on a Java 21 host.
4. Commit implementation before running `tools/benchmark.ps1`; the script rejects dirty provenance.
5. Publish only after CI succeeds on the exact `main` SHA.

## Decisions Not To Reopen

- Keep Java for interoperability with Kotlin services in the macro.
- Keep messaging `none`; no async behavior exists here.
- Do not restore simulated database claims or tracked `.gradle` caches.
- Do not claim atomic Redis/PostgreSQL writes or universal strategy superiority.
