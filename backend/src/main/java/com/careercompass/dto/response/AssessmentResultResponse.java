package com.careercompass.dto.response;

import java.util.Map;
import java.util.UUID;

public record AssessmentResultResponse(
        UUID sessionId,
        String status,
        Map<String, Double> rawScores,
        Map<String, Double> normalizedScores,
        int evidenceCreated
) {
}
