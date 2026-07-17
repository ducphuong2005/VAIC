package com.careercompass.dto.response;

import java.util.List;
import java.util.UUID;

public record AssessmentDetailResponse(
        UUID id,
        String code,
        String title,
        String description,
        String assessmentType,
        int estimatedMinutes,
        List<AssessmentQuestionResponse> questions
) {
}
