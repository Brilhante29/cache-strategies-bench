# Agent Handoff

Project: `19 - cache-strategies-bench`

## Principal Agent Summary

- Objective: Implement cache-aside vs write-through benchmark with reproducible hit_ratio and p95_latency_ms metrics.
- Portfolio program: backend-reliability-platform
- Public proof claim: cache-aside vs write-through
- Primary benchmark: hit_ratio, p95_latency_ms
- Default runnable path: `docker run --rm cache-strategies-bench`

## Subagent Decisions

| Role | Decision | Evidence Path | Status |
|---|---|---|---|
| `program-planner` | backend-reliability-platform | `project.yaml`, `sdd/spec.md` | completed |
| `architecture-selector` | layered | `sdd/architecture-decision.md` | completed |
| `engineering-principles-reviewer` | SOLID applied, KISS/YAGNI documented | `project.yaml`, `sdd/technical-decision.md` | completed |
| `stack-decision-agent` | Java 21 + Spring Boot 3.4 + Gradle KTS | `project.yaml`, `sdd/technical-decision.md` | completed |
| `api-style-agent` | REST HTTP (GET/POST products) | `application/ProductController.java` | completed |
| `cloud-local-first-agent` | none needed (fully local) | Dockerfile | completed |
| `messaging-agent` | none | `sdd/technical-decision.md` | completed |
| `language-profile-agent` | Java (java.yaml profile) | repo layout, tests, tooling | completed |
| `benchmark-harness-agent` | BenchmarkRunner (CommandLineRunner) | `sdd/benchmark-plan.md`, `benchmarks/results/` | completed |
| `design-system-agent` | README header + benchmark table | `README.md` | completed |
| `security-reuse-reviewer` | no secrets, REFERENCES.md complete | `REFERENCES.md`, release checklist | completed |
| `release-ci-publisher` | Docker + GitHub Actions CI | `.github/workflows/ci.yml` | completed |

## Local-First Runtime

- Docker command: `docker run --rm cache-strategies-bench`
- Local services: none
- Kumo services: none
- Real cloud adapter target: none
- Config switch: none
- Default path requires paid secret: no

## Architecture Boundaries

- Domain boundaries: Product entity, ProductRepository port, CacheStrategy interface + implementations
- Use-case boundaries: get product, create product, run benchmark
- Ports: ProductRepository (interface)
- Adapters: InMemoryProductStore (implements ProductRepository), InMemoryCache
- Dependency direction rule: domain depends on application (InMemoryCache); benchmark depends on application

## Benchmark Handoff

- Metric: hit_ratio, p95_latency_ms
- Unit: %, ms
- Higher or lower is better: higher hit_ratio is better; lower p95_latency_ms is better
- Command: `docker run --rm cache-strategies-bench`
- Result path: `benchmarks/results/benchmark-result.json`
- Dataset or fixture: 100 synthetic products, seed 42

## Open Risks

- Performance numbers vary by host; always regenerate benchmarks on target hardware

## Publication Gates

- [x] Docker path works
- [x] benchmark result exists
- [x] README starts with number, claim, and benchmark
- [x] references are documented
- [x] no secret in files or git remote
- [x] validation passes
