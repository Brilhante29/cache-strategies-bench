package com.portfolio.cachebench.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.portfolio.cachebench.application.CacheBenchService;
import com.portfolio.cachebench.domain.CacheStrategy;
import com.portfolio.cachebench.domain.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "benchmark.enabled", havingValue = "true", matchIfMissing = true)
public class BenchmarkRunner implements CommandLineRunner {

    static final int PRODUCT_COUNT = 100;
    static final double READ_RATIO = 0.8;
    static final long SEED = 42;

    private final CacheBenchService service;
    private final ObjectMapper mapper;
    private final int repeats;
    private final int warmupOperations;
    private final int measuredOperations;
    private final Path resultPath;

    BenchmarkRunner(CacheBenchService service) {
        this(service, new ObjectMapper(), 3, 200, 2_000,
                "benchmarks/results/cache-strategies-v2.json");
    }

    @Autowired
    public BenchmarkRunner(
            CacheBenchService service,
            ObjectMapper mapper,
            @Value("${benchmark.repeats:3}") int repeats,
            @Value("${benchmark.warmup-operations:200}") int warmupOperations,
            @Value("${benchmark.measured-operations:2000}") int measuredOperations,
            @Value("${benchmark.result-path:benchmarks/results/cache-strategies-v2.json}") String resultPath) {
        this.service = service;
        this.mapper = mapper.copy().enable(SerializationFeature.INDENT_OUTPUT);
        this.repeats = repeats;
        this.warmupOperations = warmupOperations;
        this.measuredOperations = measuredOperations;
        this.resultPath = Path.of(resultPath);
    }

    @Override
    public void run(String... args) throws Exception {
        Instant startedAt = Instant.now();
        long startedNanos = System.nanoTime();
        List<StrategyResult> cacheAside = new ArrayList<>();
        List<StrategyResult> writeThrough = new ArrayList<>();

        for (int repeat = 0; repeat < repeats; repeat++) {
            cacheAside.add(runOnce("cache-aside", repeat));
            writeThrough.add(runOnce("write-through", repeat));
        }

        var metrics = List.of(
                metric("cache_aside_hit_ratio", cacheAside, StrategyResult::getHitRatio,
                        "percent", "higher_is_better"),
                metric("write_through_hit_ratio", writeThrough, StrategyResult::getHitRatio,
                        "percent", "higher_is_better"),
                metric("cache_aside_p95_latency_ms", cacheAside, StrategyResult::getP95LatencyMs,
                        "milliseconds", "lower_is_better"),
                metric("write_through_p95_latency_ms", writeThrough, StrategyResult::getP95LatencyMs,
                        "milliseconds", "lower_is_better"),
                metric("cache_aside_p99_latency_ms", cacheAside, StrategyResult::getP99LatencyMs,
                        "milliseconds", "lower_is_better"),
                metric("write_through_p99_latency_ms", writeThrough, StrategyResult::getP99LatencyMs,
                        "milliseconds", "lower_is_better"),
                metric("cache_aside_throughput_ops_s", cacheAside, StrategyResult::getThroughputOpsPerSecond,
                        "operations_per_second", "higher_is_better"),
                metric("write_through_throughput_ops_s", writeThrough, StrategyResult::getThroughputOpsPerSecond,
                        "operations_per_second", "higher_is_better"),
                failureMetric("consistency_failures", cacheAside, writeThrough)
        );

        String fixture = "products=100;seed=42";
        String config = "reads=0.8;warmup=" + warmupOperations + ";operations=" + measuredOperations
                + ";repeats=" + repeats + ";postgres=16;redis=7";
        String artifactDigest = sha256(mapper.writeValueAsBytes(metrics));
        double durationSeconds = (System.nanoTime() - startedNanos) / 1_000_000_000.0;
        String sourceCommit = env("SOURCE_COMMIT", "0000000000000000000000000000000000000000");

        BenchmarkResultV2 result = new BenchmarkResultV2(
                2,
                UUID.randomUUID().toString(),
                "cache-strategies-bench",
                "redis-postgres-cache-strategies",
                new BenchmarkResultV2.Workload("2.0", sha256(fixture.getBytes(StandardCharsets.UTF_8)),
                        sha256(config.getBytes(StandardCharsets.UTF_8)), warmupOperations,
                        measuredOperations * repeats * 2, 1),
                metrics,
                new BenchmarkResultV2.Execution(
                        "powershell -File tools/benchmark.ps1", startedAt.toString(),
                        durationSeconds, 0, repeats),
                environment(),
                new BenchmarkResultV2.Provenance(
                        sourceCommit.matches("[0-9a-f]{40}") ? sourceCommit : "0000000000000000000000000000000000000000",
                        true,
                        env("IMAGE_REF", "cache-strategies-bench:local"),
                        normalizedDigest(env("IMAGE_DIGEST", "")),
                        normalizedDigest(env("DEPENDENCY_LOCK_DIGEST", "")),
                        env("BENCHMARK_PRODUCER", "local"),
                        artifactDigest),
                "cache-strategies:redis7:postgres16:java21:80r20w:v2");

        Files.createDirectories(resultPath.toAbsolutePath().getParent());
        mapper.writeValue(resultPath.toFile(), result);
        System.out.println(mapper.writeValueAsString(result));
    }

    private StrategyResult runOnce(String name, int repeat) {
        service.clearCache();
        service.loadTestData(PRODUCT_COUNT);
        CacheStrategy strategy = switch (name) {
            case "cache-aside" -> service.createCacheAsideStrategy();
            case "write-through" -> service.createWriteThroughStrategy();
            default -> throw new IllegalArgumentException("unknown strategy " + name);
        };

        if (name.equals("write-through")) {
            for (long id = 1; id <= PRODUCT_COUNT; id++) {
                strategy.save(new Product(id, "Product-" + id, 10.0 + id, 100 + (int) id));
            }
        }
        Random warmup = new Random(SEED + repeat);
        for (int index = 0; index < warmupOperations; index++) {
            strategy.get(1L + warmup.nextInt(PRODUCT_COUNT));
        }

        strategy.resetMetrics();
        Random workload = new Random(SEED + repeat);
        List<Long> latencies = new ArrayList<>(measuredOperations);
        long start = System.nanoTime();
        for (int index = 0; index < measuredOperations; index++) {
            long id = 1L + workload.nextInt(PRODUCT_COUNT);
            long operationStart = System.nanoTime();
            if (workload.nextDouble() < READ_RATIO) {
                strategy.get(id);
            } else {
                strategy.save(new Product(id, "Updated-" + index, 20.0 + id, 200 + (int) id));
            }
            latencies.add(System.nanoTime() - operationStart);
        }
        long durationNanos = System.nanoTime() - start;
        latencies.sort(Long::compareTo);
        long hits = strategy.getHits();
        long misses = strategy.getMisses();
        double hitRatio = hits + misses == 0 ? 0.0 : 100.0 * hits / (hits + misses);
        int consistencyFailures = verifyConsistency();

        return new StrategyResult(name, hitRatio, percentile(latencies, 0.95), percentile(latencies, 0.99),
                measuredOperations / (durationNanos / 1_000_000_000.0), consistencyFailures,
                hits, misses, measuredOperations, durationNanos / 1_000_000);
    }

    private int verifyConsistency() {
        int failures = 0;
        for (long id = 1; id <= PRODUCT_COUNT; id++) {
            Product cached = service.getCache().get(id);
            Product stored = service.getStore().findById(id).orElse(null);
            if (cached != null && (stored == null || !sameValue(cached, stored))) failures++;
        }
        return failures;
    }

    private static boolean sameValue(Product left, Product right) {
        return left.getId().equals(right.getId())
                && left.getName().equals(right.getName())
                && Double.compare(left.getPrice(), right.getPrice()) == 0
                && left.getStock() == right.getStock();
    }

    private BenchmarkResultV2.Metric metric(
            String name,
            List<StrategyResult> results,
            MetricValue value,
            String unit,
            String direction) {
        List<Double> samples = results.stream().map(value::read).toList();
        List<Double> sorted = samples.stream().sorted().toList();
        double median = sorted.get(sorted.size() / 2);
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("min", sorted.getFirst());
        summary.put("median", median);
        summary.put("max", sorted.getLast());
        return new BenchmarkResultV2.Metric(name, median, unit, direction, samples, 0, summary);
    }

    private BenchmarkResultV2.Metric failureMetric(
            String name,
            List<StrategyResult> cacheAside,
            List<StrategyResult> writeThrough) {
        List<Double> samples = new ArrayList<>();
        cacheAside.forEach(result -> samples.add((double) result.getConsistencyFailures()));
        writeThrough.forEach(result -> samples.add((double) result.getConsistencyFailures()));
        int failures = samples.stream().mapToInt(Double::intValue).sum();
        return new BenchmarkResultV2.Metric(name, failures, "count", "target", samples, failures,
                Map.of("target", 0));
    }

    private static double percentile(List<Long> values, double percentile) {
        int index = Math.min(values.size() - 1, (int) Math.ceil(percentile * values.size()) - 1);
        return values.get(index) / 1_000_000.0;
    }

    private static Map<String, Object> environment() {
        Map<String, Object> environment = new LinkedHashMap<>();
        environment.put("runtime", "Java " + System.getProperty("java.version"));
        environment.put("architecture", System.getProperty("os.arch", "unknown"));
        environment.put("hardware_class", env("HARDWARE_CLASS", "local-docker"));
        environment.put("database", "PostgreSQL 16");
        environment.put("cache", "Redis 7");
        environment.put("processors", Runtime.getRuntime().availableProcessors());
        return environment;
    }

    private static String sha256(byte[] bytes) {
        try {
            return "sha256:" + HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        } catch (Exception exception) {
            throw new IllegalStateException("SHA-256 unavailable", exception);
        }
    }

    private static String normalizedDigest(String value) {
        return value.matches("sha256:[0-9a-f]{64}")
                ? value
                : "sha256:" + "0".repeat(64);
    }

    private static String env(String name, String fallback) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    @FunctionalInterface
    private interface MetricValue {
        double read(StrategyResult result);
    }
}
