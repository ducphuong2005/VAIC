package com.careercompass.dto.response;

import java.util.List;
import java.util.UUID;

public record MiniGameResultResponse(
        UUID sessionId,
        String status,
        String resultSummary,
        List<MiniGameResultMetricResponse> metrics,
        int evidenceCreated,
        AiCareerAdviceResponse advice
) {
}
