# Benchmark Plan: cache-strategies-bench

## Hypothesis

cache-aside vs write-through, measured by hit_ratio and p95_latency_ms under a mixed read/write workload.

Cache-aside should show lower hit ratio but more consistent p95 latency because misses trigger DB reads that populate the cache. Write-through should show higher hit ratio because the cache is always consistent with the DB on writes, but may have higher p95 on writes due to dual writes.

## Command

```bash
docker build -t cache-strategies-bench . && docker run --rm cache-strategies-bench
```

## Environment

- OS: containerized (eclipse-temurin:21-jre)
- CPU: host-dependent
- RAM: host-dependent
- Docker version: host-dependent
- Date: recorded in benchmark JSON

## Inputs

- fixture: synthetic (100 products generated in-memory)
- dataset size: 100 products
- repetitions: 2000 operations per strategy
- warmup: 200 reads (cache-aside) or 100 writes (write-through)

## Metrics

| Metric | Unit | Source | Why it matters |
|---|---|---|---:|---|
| hit_ratio | % | strategy hit/miss counters | Direct comparison of cache effectiveness |
| p95_latency_ms | ms | operation timestamps (nanosecond precision) | Real-world latency impact on 95th percentile requests |

## Result schema

Output must be JSON and include project, metric, value, unit, timestamp, environment, and command.

```json
{
  "project": "cache-strategies-bench",
  "claim": "cache-aside vs write-through",
  "timestamp": "2026-07-20T17:00:00Z",
  "environment": { "os": "...", "java_version": "21" },
  "strategies": [
    { "strategy": "cache-aside", "hit_ratio": 75.0, "p95_latency_ms": 3.5 },
    { "strategy": "write-through", "hit_ratio": 95.0, "p95_latency_ms": 6.2 }
  ],
  "command": "docker run --rm cache-strategies-bench"
}
```

## Post angle

#19 cache-strategies-bench: hit_ratio, p95_latency_ms as a reproducible portfolio benchmark.
