package com.careercompass.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GenerateLearningPathRequest(
        @NotBlank String onetCode,
        String route
) {
}
