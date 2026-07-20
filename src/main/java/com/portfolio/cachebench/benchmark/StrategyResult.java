package com.portfolio.cachebench.benchmark;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StrategyResult {

    @JsonProperty("strategy")
    private String strategy;

    @JsonProperty("hit_ratio")
    private double hitRatio;

    @JsonProperty("p95_latency_ms")
    private double p95LatencyMs;

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
        this.strategy = strategy;
        this.hitRatio = hitRatio;
        this.p95LatencyMs = p95LatencyMs;
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
