package com.careercompass.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record GenerateRecommendationRequest(
        String region,
        @Min(1) @Max(50) Integer limit
) {
}
