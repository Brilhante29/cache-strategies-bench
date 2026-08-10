package com.portfolio.cachebench.benchmark;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public record BenchmarkResultV2(
        @JsonProperty("schema_version") int schemaVersion,
        @JsonProperty("run_id") String runId,
        String project,
        @JsonProperty("benchmark_id") String benchmarkId,
        Workload workload,
        List<Metric> metrics,
        Execution execution,
        Map<String, Object> environment,
        Provenance provenance,
        @JsonProperty("comparability_key") String comparabilityKey) {

    public record Workload(
            String version,
            @JsonProperty("fixture_digest") String fixtureDigest,
            @JsonProperty("config_digest") String configDigest,
            @JsonProperty("warmup_iterations") int warmupIterations,
            @JsonProperty("measured_iterations") int measuredIterations,
            int concurrency) {}

    public record Metric(
            String name,
            double value,
            String unit,
            String direction,
            List<Double> samples,
            int failures,
            Map<String, Object> summary) {}

    public record Execution(
            String command,
            @JsonProperty("started_at") String startedAt,
            @JsonProperty("duration_seconds") double durationSeconds,
            @JsonProperty("exit_code") int exitCode,
            int repeat) {}

    public record Provenance(
            @JsonProperty("source_commit") String sourceCommit,
            @JsonProperty("clean_tree") boolean cleanTree,
            @JsonProperty("image_ref") String imageRef,
            @JsonProperty("image_digest") String imageDigest,
            @JsonProperty("dependency_lock_digest") String dependencyLockDigest,
            String producer,
            @JsonProperty("artifact_digest") String artifactDigest) {}
}
