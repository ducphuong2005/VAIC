package com.careercompass.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record UpdateStepProgressRequest(@Min(0) @Max(100) int progressPercent) {
}
