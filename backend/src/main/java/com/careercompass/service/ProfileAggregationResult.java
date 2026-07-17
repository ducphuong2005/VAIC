package com.careercompass.service;

import java.math.BigDecimal;

public record ProfileAggregationResult(
        BigDecimal score,
        BigDecimal confidence,
        int evidenceCount
) {
}
