# Technical Decision

## Status

Accepted

## Decision Type

stack, library, runtime

## Context

Project: `cache-strategies-bench`
Problem: Compare cache-aside vs write-through caching strategies with reproducible hit_ratio and p95_latency_ms metrics.
Portfolio program: backend-reliability-platform
Public signal: Java + Spring Boot benchmark with architecture boundaries, unit tests, and Docker delivery.
Benchmark: hit_ratio, p95_latency_ms

## Selected Option

Selected: Java 21 + Spring Boot 3.4 + Gradle Kotlin DSL + Jackson

Reason:

- Java 21 provides modern language features (records, pattern matching, virtual threads) and LTS stability
- Spring Boot 3.4 provides dependency injection, configuration, and CLI runner scaffold
- Gradle Kotlin DSL is the standard for modern JVM projects
- Jackson is the de facto JSON library for Java and is included in spring-boot-starter-web
- In-memory implementations remove external service dependencies, keeping the benchmark local-first

## Decision Brain Fields

- Stack profile: java
- API style: rest-http
- Messaging: none
- Cloud mode: none
- Database/runtime: in-memory simulation / Docker
- Library policy: minimal — Spring Boot starter-web, Jackson. No persistence, caching, or messaging libraries.

## Engineering Principles

Coupling boundary:

Domain (Product, ProductRepository, CacheStrategy) does not depend on framework. Strategy implementations reference InMemoryCache (application layer) and ProductRepository (domain interface).

SOLID application:

- SRP: Each strategy has one responsibility (get/put/evict with specific semantics)
- OCP: New strategies implement CacheStrategy without modifying existing code
- LSP: CacheAsideStrategy and WriteThroughStrategy are substitutable for CacheStrategy
- ISP: CacheStrategy interface exposes minimal surface area (get, save, evict, getName, metrics)
- DIP: BenchmarkRunner depends on CacheStrategy abstraction, not concrete implementations

Simplicity:

- KISS: In-memory cache with ConcurrentHashMap + TTL. No distributed cache, no eviction policies.
- YAGNI: No metrics export (Micrometer), no health checks, no distributed tracing
- DRY: Strategy code is intentionally not shared — the point is to show different behaviors

Testability evidence:

- CacheAsideStrategyTest + WriteThroughStrategyTest: pure Java, no Spring, instant setup
- InMemoryCacheTest: no dependencies, fast assertions
- BenchmarkRunnerTest: Jackson serialization, service wiring

## Rejected Options

| Option | Why rejected |
|---|---|
| Kotlin | Java is the primary profile; Kotlin adds unnecessary language complexity |
| Redis + PostgreSQL | Requires Docker Compose, increases build time, adds no value to strategy comparison |
| WebFlux | Blocking I/O matches the simulated DB latency; reactive adds complexity without benefit |
| Spring Data JPA | No real database needed; simulated latency is controlled and deterministic |
| Maven | Gradle Kotlin DSL is the standard for modern Spring Boot projects in this portfolio |

## API Contract

Contract artifact: OpenAPI (implicit — two endpoints documented in code)

REST API (for interactive use):

- `GET /api/products/{id}` — get product by ID using current strategy
- `POST /api/products` — create product using current strategy
- `POST /api/products/strategy/{name}` — switch active strategy

## Cloud Local-First

Local provider: none (fully local, no cloud dependencies)

Config switch: none

Unsupported local behaviors: none

## Benchmark Impact

Expected impact:

- Clear comparison of hit_ratio under 80/20 read/write workload
- P95 latency shows the cost of cache misses (DB reads) vs cache hits
- Deterministic seed ensures reproducibility across runs

Validation command:

```powershell
docker build -t cache-strategies-bench .; docker run --rm cache-strategies-bench
```

## Operational Cost

- Docker services added: none (single-stage: build + run in one container)
- Local demo complexity: low
- Failure case required: no

## Follow-up

- If benchmark shows unexpected results, verify warmup phase and cache TTL
- If hit ratio is identical, verify workload distribution targets both strategies fairly
