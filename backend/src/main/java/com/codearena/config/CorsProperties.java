package com.codearena.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "codearena.cors")
public record CorsProperties(String allowedOrigins) {
}
