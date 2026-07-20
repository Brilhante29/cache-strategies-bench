# Architecture Decision

## Status

Accepted

## Context

Project: `cache-strategies-bench`
Claim: `cache-aside vs write-through — compare caching strategies`
Benchmark: `hit_ratio, p95_latency_ms`

Problem forces:

- Domain complexity: low
- Integration pressure: low
- UI state complexity: none
- Data/ML reproducibility: medium
- Auditability/event history: low
- Throughput/async pressure: low
- Independent deployability need: low

## Decision

Chosen architecture: `layered`

Reason:

The problem is a simple microbenchmark comparing two cache strategies. A layered
architecture with domain, application, and benchmark layers is sufficient. No
external adapters, no database migrations, no event bus. The layers map directly
to the package structure.

Dependency rule:

Domain and application classes have no framework dependency beyond Spring
stereotype annotations (@Component, @Service). Benchmark orchestration lives in
the benchmark layer and depends on application services.

## Rejected Alternatives

| Alternative | Why rejected |
|---|---|
| hexagonal | Overkill — no adapter swapping, single in-memory implementation for both cache and store |
| clean-architecture | Extra ceremony (use cases, ports) would obscure the benchmark result |
| MVC | No views, no templates, no HTTP-rendered UI |

## Folder Layout

```
src/main/java/com/portfolio/cachebench/
  CacheBenchApplication.java
  domain/
  application/
  benchmark/
src/test/java/com/portfolio/cachebench/
  domain/
  benchmark/
```

## Testing Strategy

- Unit tests: Strategy implementations (pure Java, no Spring), InMemoryCache
- Integration tests: Benchmark runner wiring, JSON serialization
- Benchmark: CommandLineRunner runs on startup, outputs JSON to stdout and file

## Consequences

Positive:

- Simple, testable, easy to understand
- No external dependencies needed for build or run
- Benchmark is self-contained in a single JAR

Tradeoffs:

- In-memory simulation may not reflect real Redis + PostgreSQL performance
- Results are relative comparison, not absolute numbers for production

Migration path:

- Replace InMemoryCache with Redis binding (Jedis/Lettuce)
- Replace InMemoryProductStore with Spring Data JPA + PostgreSQL
- Add Spring Web to enable REST API alongside benchmark
