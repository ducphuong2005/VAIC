package com.careercompass.dto.response;

import java.time.Instant;

public record RecommendationFeedbackResponse(
        Long id,
        Long recommendationId,
        Integer rating,
        String feedbackText,
        Instant createdAt
) {
}
