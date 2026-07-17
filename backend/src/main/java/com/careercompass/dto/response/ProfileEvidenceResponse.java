package com.careercompass.dto.response;

import java.math.BigDecimal;
import java.time.Instant;

public record ProfileEvidenceResponse(
        Long id,
        String sourceType,
        String sourceId,
        String elementId,
        BigDecimal evidenceScore,
        BigDecimal evidenceConfidence,
        String evidencePayload,
        Instant createdAt
) {
}
