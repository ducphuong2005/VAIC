package com.careercompass.dto.response;

import java.util.UUID;

public record AssessmentProgressResponse(
        UUID assessmentId,
        String title,
        String status,
        int progressPercent
) {
}
