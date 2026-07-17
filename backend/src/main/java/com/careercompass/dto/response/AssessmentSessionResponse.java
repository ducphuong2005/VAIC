package com.careercompass.dto.response;

import java.time.Instant;
import java.util.UUID;

public record AssessmentSessionResponse(
        UUID sessionId,
        UUID assessmentId,
        String status,
        Instant startedAt,
        Instant completedAt
) {
}
