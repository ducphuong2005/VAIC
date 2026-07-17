package com.careercompass.dto.response;

import java.util.UUID;

public record AssessmentSummaryResponse(
        UUID id,
        String code,
        String title,
        String description,
        String assessmentType,
        int estimatedMinutes
) {
}
