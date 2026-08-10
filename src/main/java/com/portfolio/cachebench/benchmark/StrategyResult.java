package com.portfolio.cachebench.benchmark;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StrategyResult {

    @JsonProperty("strategy")
    private String strategy;

    @JsonProperty("hit_ratio")
    private double hitRatio;

    @JsonProperty("p95_latency_ms")
    private double p95LatencyMs;

    @JsonProperty("p99_latency_ms")
    private double p99LatencyMs;

    @JsonProperty("throughput_ops_s")
    private double throughputOpsPerSecond;

    @JsonProperty("consistency_failures")
    private int consistencyFailures;

    @JsonProperty("hits")
    private long hits;

    @JsonProperty("misses")
    private long misses;

    @JsonProperty("total_operations")
    private long totalOperations;

    @JsonProperty("total_time_ms")
    private long totalTimeMs;

    public StrategyResult() {
    }

    public StrategyResult(String strategy, double hitRatio, double p95LatencyMs,
                          long hits, long misses, long totalOperations, long totalTimeMs) {
        this(strategy, hitRatio, p95LatencyMs, p95LatencyMs, 0.0, 0,
                hits, misses, totalOperations, totalTimeMs);
    }

    public StrategyResult(String strategy, double hitRatio, double p95LatencyMs,
                          double p99LatencyMs, double throughputOpsPerSecond,
                          int consistencyFailures, long hits, long misses,
                          long totalOperations, long totalTimeMs) {
        this.strategy = strategy;
        this.hitRatio = hitRatio;
        this.p95LatencyMs = p95LatencyMs;
        this.p99LatencyMs = p99LatencyMs;
        this.throughputOpsPerSecond = throughputOpsPerSecond;
        this.consistencyFailures = consistencyFailures;
        this.hits = hits;
        this.misses = misses;
        this.totalOperations = totalOperations;
        this.totalTimeMs = totalTimeMs;
    }

    public String getStrategy() {
        return strategy;
    }

    public void setStrategy(String strategy) {
        this.strategy = strategy;
    }

    public double getHitRatio() {
        return hitRatio;
    }

    public void setHitRatio(double hitRatio) {
        this.hitRatio = hitRatio;
    }

    public double getP95LatencyMs() {
        return p95LatencyMs;
    }

    public void setP95LatencyMs(double p95LatencyMs) {
        this.p95LatencyMs = p95LatencyMs;
    }

    public double getP99LatencyMs() {
        return p99LatencyMs;
    }

    public void setP99LatencyMs(double p99LatencyMs) {
        this.p99LatencyMs = p99LatencyMs;
    }

    public double getThroughputOpsPerSecond() {
        return throughputOpsPerSecond;
    }

    public void setThroughputOpsPerSecond(double throughputOpsPerSecond) {
        this.throughputOpsPerSecond = throughputOpsPerSecond;
    }

    public int getConsistencyFailures() {
        return consistencyFailures;
    }

    public void setConsistencyFailures(int consistencyFailures) {
        this.consistencyFailures = consistencyFailures;
    }

    public long getHits() {
        return hits;
    }

    public void setHits(long hits) {
        this.hits = hits;
    }

    public long getMisses() {
        return misses;
    }

    public void setMisses(long misses) {
        this.misses = misses;
    }

    public long getTotalOperations() {
        return totalOperations;
    }

    public void setTotalOperations(long totalOperations) {
        this.totalOperations = totalOperations;
    }

    public long getTotalTimeMs() {
        return totalTimeMs;
    }

    public void setTotalTimeMs(long totalTimeMs) {
        this.totalTimeMs = totalTimeMs;
    }
}
