package com.portfolio.cachebench.benchmark;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class BenchmarkResult {

    @JsonProperty("project")
    private String project;

    @JsonProperty("claim")
    private String claim;

    @JsonProperty("timestamp")
    private String timestamp;

    @JsonProperty("environment")
    private Map<String, String> environment;

    @JsonProperty("strategies")
    private List<StrategyResult> strategies;

    @JsonProperty("command")
    private String command;

    public BenchmarkResult() {
    }

    public BenchmarkResult(String project, String claim, List<StrategyResult> strategies,
                           Map<String, String> environment, String command) {
        this.project = project;
        this.claim = claim;
        this.timestamp = Instant.now().toString();
        this.strategies = strategies;
        this.environment = environment;
        this.command = command;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getClaim() {
        return claim;
    }

    public void setClaim(String claim) {
        this.claim = claim;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public List<StrategyResult> getStrategies() {
        return strategies;
    }

    public void setStrategies(List<StrategyResult> strategies) {
        this.strategies = strategies;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
