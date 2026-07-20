# Spec: cache-strategies-bench

## Number

#19

## Claim

cache-aside vs write-through — which strategy yields higher hit ratio and lower P95 latency under a mixed read/write workload?

## Stack

java21, spring-boot, docker

## User-visible output

- Docker command: `docker run --rm cache-strategies-bench`
- README opens with: # #19 cache-strategies-bench
- Benchmark table: hit_ratio, p95_latency_ms

## Scope

In:

- In-memory cache + simulated DB with configurable latency
- Two cache strategies (cache-aside, write-through)
- Configurable workload: N operations, M products, read/write ratio
- Reproducible JSON benchmark result
- Docker multi-stage build

Out:

- Real Redis, PostgreSQL, or any external service
- Kubernetes, compose, or multi-container orchestration
- Web UI or dashboard
- Paid credentials or secrets

## Architecture

```
client -> app -> domain -> adapters -> benchmark output
```

## Benchmark

Primary metric:

- name: hit_ratio, p95_latency_ms
- target: first reproducible baseline
- command: `docker run --rm cache-strategies-bench`
- result file: `benchmarks/results/benchmark-result.json`

## Dataset or fixture

- source: synthetic (generated in-memory)
- size: 100 products
- license: MIT
- deterministic seed: 42

## Definition of done

- [x] Docker command works from clean clone.
- [x] README starts with project number and benchmark result.
- [x] Benchmark command writes JSON result.
- [x] Tests cover core behavior.
- [x] REFERENCES.md explains reuse.
- [x] No secret or paid credential required for default demo.
