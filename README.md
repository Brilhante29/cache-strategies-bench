# #19 cache-strategies-bench

**Status:** benchmarked

**Proves:** cache-aside vs write-through.

**Benchmark target:** hit_ratio, p95_latency_ms.

**Stack:** java21, spring-boot, docker.

## Result

| Strategy | Hit Ratio (%) | P95 Latency (ms) | Total Time (ms) |
|---|---|---|---|
| cache-aside | 80.3 | 5.21 | 2904 |
| write-through | 100.0 | 5.12 | 1508 |

**Winner:** write-through achieves 100% hit ratio and ~2x faster total time under a 80/20 read/write workload (100 products, 2000 ops, seed 42). Cache-aside pays a cache-miss penalty on every read after a write due to cache eviction (DS/read-store DB round-trip). Write-through maintains cache coherency on every write, eliminating read-time cache misses at the cost of write-time dual writes.

Run `docker run --rm cache-strategies-bench` to generate fresh results on your hardware.

## Run

```bash
docker build -t cache-strategies-bench .
docker run --rm cache-strategies-bench
```

## Benchmark

```bash
# Build and run (same command):
docker build -t cache-strategies-bench . && docker run --rm cache-strategies-bench
```

Output: `benchmarks/results/benchmark-result.json`

## Architecture

```
src/main/java/com/portfolio/cachebench/
  CacheBenchApplication.java          - Spring Boot entry point
  domain/
    Product.java                      - Entity: id, name, price, stock
    ProductRepository.java            - Port interface
    CacheStrategy.java                - Strategy interface
    CacheAsideStrategy.java           - Read: cache -> miss -> DB -> store. Write: DB -> evict.
    WriteThroughStrategy.java         - Read: cache only. Write: DB + cache.
  application/
    InMemoryCache.java                - ConcurrentHashMap with TTL
    InMemoryProductStore.java         - Simulated DB with 1-5ms random latency
    CacheBenchService.java            - Service orchestrator
    ProductController.java            - REST endpoints
  benchmark/
    BenchmarkRunner.java              - CommandLineRunner, runs workload, records metrics
    BenchmarkResult.java              - JSON output schema
    StrategyResult.java               - Per-strategy metrics schema
```

Dependency direction: domain -> application -> benchmark. All in single JAR.

## Test

```bash
./gradlew test
```

## References

See REFERENCES.md.

## License

MIT
