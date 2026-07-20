package com.portfolio.cachebench.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.portfolio.cachebench.application.CacheBenchService;
import com.portfolio.cachebench.domain.CacheStrategy;
import com.portfolio.cachebench.domain.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Component
public class BenchmarkRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BenchmarkRunner.class);

    static final int PRODUCT_COUNT = 100;
    static final int WARMUP_OPERATIONS = 200;
    static final int WORKLOAD_OPERATIONS = 2000;
    static final double READ_RATIO = 0.8;
    static final long SEED = 42;

    private final CacheBenchService benchService;
    private final ObjectMapper mapper = new ObjectMapper();

    public BenchmarkRunner(CacheBenchService benchService) {
        this.benchService = benchService;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("=== Cache Strategies Benchmark ===");
        log.info("Loading {} test products...", PRODUCT_COUNT);
        benchService.loadTestData(PRODUCT_COUNT);
        log.info("Loaded {} products", benchService.getStore().count());

        List<StrategyResult> results = new ArrayList<>();

        results.add(runBenchmark("cache-aside", this::warmCacheAside));
        results.add(runBenchmark("write-through", this::warmWriteThrough));

        Map<String, String> env = new LinkedHashMap<>();
        env.put("os", System.getProperty("os.name", "unknown"));
        env.put("arch", System.getProperty("os.arch", "unknown"));
        env.put("java_version", System.getProperty("java.version", "unknown"));
        env.put("available_processors", String.valueOf(Runtime.getRuntime().availableProcessors()));
        env.put("max_memory_mb", String.valueOf(Runtime.getRuntime().maxMemory() / 1024 / 1024));

        BenchmarkResult result = new BenchmarkResult(
                "cache-strategies-bench",
                "cache-aside vs write-through (hit_ratio, p95_latency_ms)",
                results,
                env,
                "docker run --rm cache-strategies-bench"
        );

        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        String json = mapper.writeValueAsString(result);

        log.info("=== BENCHMARK RESULT ===");
        System.out.println(json);

        Path outputDir = Paths.get("benchmarks", "results");
        Files.createDirectories(outputDir);
        Files.writeString(outputDir.resolve("benchmark-result.json"), json);
        log.info("Result written to benchmarks/results/benchmark-result.json");

        log.info("=== BENCHMARK COMPLETE ===");
    }

    private StrategyResult runBenchmark(String label, WarmupStrategy warmup) {
        log.info("--- {} ---", label);
        benchService.clearCache();

        CacheStrategy strategy = switch (label) {
            case "cache-aside" -> benchService.createCacheAsideStrategy();
            case "write-through" -> benchService.createWriteThroughStrategy();
            default -> throw new IllegalArgumentException("Unknown strategy: " + label);
        };

        strategy.resetMetrics();
        warmup.warm(strategy);
        log.info("Warmup complete. Cache size: {}", benchService.getCache().size());

        strategy.resetMetrics();
        Random random = new Random(SEED);
        List<Long> latenciesNanos = new ArrayList<>(WORKLOAD_OPERATIONS);

        long startTime = System.nanoTime();
        for (int i = 0; i < WORKLOAD_OPERATIONS; i++) {
            long productId = 1 + random.nextInt(PRODUCT_COUNT);
            boolean isRead = random.nextDouble() < READ_RATIO;

            long opStart = System.nanoTime();
            if (isRead) {
                strategy.get(productId);
            } else {
                Product p = new Product(productId, "Updated-" + productId, 20.0 + productId, 200 + (int) productId);
                strategy.save(p);
            }
            long opEnd = System.nanoTime();
            latenciesNanos.add(opEnd - opStart);
        }
        long endTime = System.nanoTime();
        long totalTimeMs = (endTime - startTime) / 1_000_000;

        long hits = strategy.getHits();
        long misses = strategy.getMisses();
        long totalLookups = hits + misses;
        double hitRatio = totalLookups > 0 ? (double) hits / totalLookups * 100.0 : 0.0;

        latenciesNanos.sort(Long::compareTo);
        int p95Index = (int) Math.ceil(0.95 * latenciesNanos.size()) - 1;
        double p95LatencyMs = latenciesNanos.get(Math.min(p95Index, latenciesNanos.size() - 1)) / 1_000_000.0;

        log.info("Hits: {}, Misses: {}, Hit Ratio: {}%", hits, misses, String.format("%.1f", hitRatio));
        log.info("P95 Latency: {} ms", String.format("%.2f", p95LatencyMs));
        log.info("Total time: {} ms", totalTimeMs);

        return new StrategyResult(label, hitRatio, p95LatencyMs,
                hits, misses, WORKLOAD_OPERATIONS, totalTimeMs);
    }

    private void warmCacheAside(CacheStrategy strategy) {
        Random random = new Random(SEED);
        for (int i = 0; i < WARMUP_OPERATIONS; i++) {
            long id = 1 + random.nextInt(PRODUCT_COUNT);
            strategy.get(id);
        }
    }

    private void warmWriteThrough(CacheStrategy strategy) {
        for (int i = 1; i <= PRODUCT_COUNT; i++) {
            Product p = new Product((long) i, "Warm-" + i, 15.0 + i, 150 + i);
            strategy.save(p);
        }
    }

    @FunctionalInterface
    interface WarmupStrategy {
        void warm(CacheStrategy strategy);
    }
}
