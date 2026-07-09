# #19 cache-strategies-bench

**Status:** scaffold

**Proves:** cache-aside vs write-through.

**Benchmark target:** hit_ratio, p95_latency_ms.

**Stack:** java21, spring-boot, redis, postgresql, k6, docker.

## Next milestone

Implement the smallest Docker-runnable version and produce the first JSON benchmark under enchmarks/results/.

## Run

`ash
docker build -t cache-strategies-bench .
docker run --rm cache-strategies-bench
`

## Benchmark

`ash
docker run --rm cache-strategies-bench benchmark
`

| Metric | Value | Unit |
|---|---:|---|
| hit_ratio, p95_latency_ms | pending | pending |

## Architecture

Defined in sdd/spec.md before implementation.

## References

See REFERENCES.md.