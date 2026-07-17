package com.careercompass.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.security.internal")
public record InternalApiProperties(@NotBlank String crawlerApiKey) {
}
