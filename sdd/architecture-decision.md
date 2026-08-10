# Architecture Decision

## Status

Accepted: hexagonal architecture.

## Context

The proof depends on substituting fast test doubles with Redis and PostgreSQL while keeping cache strategy behavior unchanged. The former layered implementation inverted this direction by importing `InMemoryCache` from domain classes.

## Decision

`CacheAsideStrategy` and `WriteThroughStrategy` depend on `ProductCache` and `ProductRepository` output ports. Redis and JDBC are adapters. The benchmark composes those ports through Spring but owns no infrastructure logic.

```text
benchmark -> domain strategies -> domain ports <- Redis/JDBC adapters
```

## Rejected

| Alternative | Reason |
|---|---|
| layered MVC | no HTTP/UI force and it did not protect dependency direction |
| microservices | one bounded benchmark does not need network deployment boundaries |
| CQRS/event sourcing | PostgreSQL remains the source of truth; history/replay is proved by #14 |

## Consequences

Unit tests remain fast with in-memory adapters. Compose is required for the portfolio benchmark because simulated latency cannot support Redis/PostgreSQL performance claims.
