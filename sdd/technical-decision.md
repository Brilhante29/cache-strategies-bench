# Technical Decision

## Selected Stack

Java 21, Spring Boot 3.4, Spring JDBC, Spring Data Redis/Lettuce, PostgreSQL 16, Redis 7, Flyway, Jackson, Gradle 8.10, Docker Compose.

## Reasons

- JDBC keeps reads and upserts visible; JPA would hide behavior without improving the proof.
- Lettuce is managed by Spring Data Redis and supports bounded TTL operations.
- Compose provides local-first service parity without credentials. Managed Redis/PostgreSQL remain configuration-only adapter replacements.
- No REST/GraphQL is exposed because the caller is the benchmark harness.
- No Kafka/RabbitMQ is used because there is no asynchronous semantic to prove.
- Kumo is not applicable: no AWS API behavior appears in the claim.

## Engineering Principles

- SRP: strategies, ports, adapters, benchmark production, and validation have separate ownership.
- OCP/LSP: in-memory and real implementations obey the same port contracts.
- ISP/DIP: domain code sees only five cache operations and five repository operations.
- KISS: one product, two strategies, two services, one workload.
- YAGNI: no ORM, API, broker, cloud SDK, tracing stack, or generalized cache abstraction.

## Failure Semantics

Cache-aside persists before eviction. Write-through persists before cache update. A cache failure can surface after PostgreSQL succeeds, so this repository claims measured consistency during healthy runs, not atomic dual writes. The outbox and saga repositories cover distributed recovery separately.
