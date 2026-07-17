package com.careercompass.dto.request;

import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.Map;

public record CompleteMiniGameRequest(
        @NotEmpty Map<String, BigDecimal> rawMetrics,
        String resultSummary
) {
}
