# Reuse Improvement Review

Project: `19 - cache-strategies-bench`

## Review Points

- [x] after scaffold
- [x] after architecture decision
- [x] after first working slice
- [x] after benchmark result
- [x] before publication
- [ ] after CI failure, if applicable

## Findings

| Finding | Classification | Kit Area | Action | Status |
|---|---|---|---|---|
| Java language profile missing `spring-boot` in applies_to | patch_now | language-profiles/java.yaml | Add `spring-boot` to java profile | accepted |
| In-memory cache + simulated DB pattern is reusable for other latency benchmarks | backlog | harness | Create in-memory latency simulator template | pending |
| CommandLineRunner benchmark pattern is well-documented in this project | reject | templates | Pattern is too project-specific for reuse | rejected |

## Patch Now Decisions

- Patch `java.yaml` language profile to add `spring-boot` to `applies_to` list (done in `.portfolio/language-profiles/java.yaml`)

## Backlog Decisions

- Consider creating an in-memory latency simulator template in `portfolio-reuse-kit/harness/` for future latency benchmarks

## Rejected Improvements

- CommandLineRunner benchmark pattern is too tied to Spring Boot to be a standalone template

## Final Gate

- [x] Reusable improvements were patched or recorded.
- [x] Project-specific implementation was not moved into the kit.
- [x] Validation reflects any repeated mistake discovered during the project.
