# Spec: #19 cache-strategies-bench

## Objective

Measure cache-aside and write-through against real Redis 7 and PostgreSQL 16 under the same deterministic 80/20 read/write workload.

## In Scope

- `ProductCache` and `ProductRepository` ports with in-memory test adapters and real Redis/JDBC adapters.
- Three repetitions with equal workload configuration and warm-up count.
- Hit ratio, p95, p99, throughput, and stale-cache consistency checks.
- Docker Compose local-first runtime and V2 benchmark artifact.

## Out Of Scope

- Production sizing, multi-region behavior, Redis durability, HTTP API, broker, ORM, and cloud SDK.

## Acceptance

- [x] Real Redis and PostgreSQL participate in the measured path.
- [x] Domain strategies contain no framework imports.
- [x] The container regenerates `benchmarks/results/cache-strategies-v2.json` through a host volume.
- [x] README opens with project number and measured values.
- [x] Default path requires no secret or paid service.
