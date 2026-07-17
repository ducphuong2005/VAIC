package com.careercompass.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.recommendation")
public record RecommendationProperties(@Valid Weights weights) {

    public record Weights(
            @DecimalMin("0") double interest,
            @DecimalMin("0") double ability,
            @DecimalMin("0") double skill,
            @DecimalMin("0") double workStyle,
            @DecimalMin("0") double market,
            @DecimalMin("0") double feasibility
    ) {
    }
}
