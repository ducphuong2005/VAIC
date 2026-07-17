package com.careercompass.dto.response;

import java.util.List;

public record AssessmentQuestionResponse(
        Long id,
        String questionText,
        int displayOrder,
        String questionType,
        List<AssessmentOptionResponse> options
) {
}
