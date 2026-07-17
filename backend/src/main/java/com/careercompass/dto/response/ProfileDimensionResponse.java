package com.careercompass.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ProfileDimensionResponse(
        String elementId,
        String elementName,
        String category,
        BigDecimal score,
        BigDecimal confidence,
        int evidenceCount,
        Instant lastCalculatedAt
) {
}
