package com.careercompass.dto.response;

import java.util.List;
import java.util.UUID;

public record RecommendationRunResponse(
        UUID runId,
        double profileConfidence,
        List<RecommendationDetailResponse> recommendations
) {
}
