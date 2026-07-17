package com.careercompass.dto.response;

import java.math.BigDecimal;

public record MiniGameResultMetricResponse(
        String metricCode,
        BigDecimal rawValue,
        BigDecimal normalizedScore
) {
}
