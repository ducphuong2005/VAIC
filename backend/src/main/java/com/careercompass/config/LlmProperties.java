package com.careercompass.config;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "llm")
public record LlmProperties(
        String provider,
        String baseUrl,
        String geminiBaseUrl,
        String apiKey,
        String geminiApiKey,
        String model,
        String contextModel,
        String answerModel,
        @Min(1) int timeoutSeconds
) {
}
