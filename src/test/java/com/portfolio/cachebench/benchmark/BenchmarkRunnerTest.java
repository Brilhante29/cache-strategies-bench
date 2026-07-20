package com.portfolio.cachebench.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfolio.cachebench.application.CacheBenchService;
import com.portfolio.cachebench.application.InMemoryCache;
import com.portfolio.cachebench.application.InMemoryProductStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BenchmarkRunnerTest {

    private CacheBenchService benchService;
    private BenchmarkRunner runner;

    @BeforeEach
    void setUp() {
        InMemoryCache cache = new InMemoryCache();
        InMemoryProductStore store = new InMemoryProductStore();
        benchService = new CacheBenchService(cache, store);
        runner = new BenchmarkRunner(benchService);
    }

    @Test
    void shouldLoadTestData() {
        benchService.loadTestData(100);
        assertEquals(100, benchService.getStore().count());
    }

    @Test
    void shouldClearCache() {
        benchService.getCache().put(1L, new com.portfolio.cachebench.domain.Product(1L, "test", 1.0, 1));
        assertEquals(1, benchService.getCache().size());
        benchService.clearCache();
        assertEquals(0, benchService.getCache().size());
    }

    @Test
    void shouldCreateCacheAsideStrategy() {
        var strategy = benchService.createCacheAsideStrategy();
        assertNotNull(strategy);
        assertEquals("cache-aside", strategy.getName());
    }

    @Test
    void shouldCreateWriteThroughStrategy() {
        var strategy = benchService.createWriteThroughStrategy();
        assertNotNull(strategy);
        assertEquals("write-through", strategy.getName());
    }

    @Test
    void benchmarkResultShouldSerializeToJson() throws Exception {
        var result = new StrategyResult("cache-aside", 85.5, 3.2, 800, 200, 1000, 5000);
        var mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(result);
        assertTrue(json.contains("cache-aside"));
        assertTrue(json.contains("hit_ratio"));
        assertTrue(json.contains("p95_latency_ms"));
    }

    @Test
    void fullBenchmarkResultShouldContainBothStrategies() throws Exception {
        var mapper = new ObjectMapper().enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);
        var cacheAside = new StrategyResult("cache-aside", 85.0, 3.5, 850, 150, 1000, 4500);
        var writeThrough = new StrategyResult("write-through", 95.0, 0.5, 950, 50, 1000, 1000);

        java.util.List<StrategyResult> strategies = java.util.List.of(cacheAside, writeThrough);
        var env = java.util.Map.of("os", "test", "java_version", "21");
        var benchmarkResult = new BenchmarkResult("cache-strategies-bench", "claim", strategies, env, "test-command");

        String json = mapper.writeValueAsString(benchmarkResult);
        assertTrue(json.contains("cache-aside"));
        assertTrue(json.contains("write-through"));
        assertTrue(json.contains("project"));
        assertTrue(json.contains("timestamp"));
        assertTrue(json.contains("environment"));
    }
}
