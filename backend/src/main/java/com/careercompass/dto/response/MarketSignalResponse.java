package com.careercompass.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record MarketSignalResponse(
        String source,
        Instant lastUpdatedAt,
        BigDecimal dataConfidence,
        Integer sampleSize,
        Integer jobCount,
        BigDecimal growthRate,
        BigDecimal medianSalary,
        BigDecimal entryLevelRatio,
        BigDecimal remoteRatio,
        BigDecimal demandScore
) {
}
