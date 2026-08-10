# Benchmark Plan

## Hypothesis

Write-through should keep a higher hit ratio under write invalidation; cache-aside should trade lower write cost for subsequent misses. Latency and throughput must be measured rather than inferred.

## Command

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File tools/benchmark.ps1
```

## Workload

- 100 products, seed 42.
- 80% reads and 20% writes.
- 200 warm-up and 2,000 measured operations per strategy.
- Three sequential repetitions; same workload generator per strategy.
- Redis 7 and PostgreSQL 16 in Docker Compose.

## Metrics

| Metric | Unit | Direction |
|---|---|---|
| hit ratio | percent | higher |
| p95 and p99 latency | milliseconds | lower |
| throughput | operations/second | higher |
| consistency failures | count | exactly zero |

The V2 artifact records samples, min/median/max, workload digests, provenance, and comparability key. Cross-host comparisons require the same key and should still report hardware differences.
