package com.careercompass.dto.response;

import java.math.BigDecimal;

public record MiniGameMetricResponse(
        String metricCode,
        String metricName,
        BigDecimal minValue,
        BigDecimal maxValue,
        boolean higherIsBetter
) {
}
