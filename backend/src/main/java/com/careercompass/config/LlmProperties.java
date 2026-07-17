package com.careercompass.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmProperties(
        String provider,
        String baseUrl,
        String apiKey,
        String model,
        @Min(1) int timeoutSeconds
) {
}
