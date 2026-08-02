package com.codearena.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "codearena.judge")
public record JudgeProperties(
        String workdir,
        int executionTimeoutSeconds,
        int memoryLimitMb,
        double cpuLimit,
        int pidsLimit
) {
}
