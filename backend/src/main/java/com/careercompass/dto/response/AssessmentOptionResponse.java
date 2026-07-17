package com.careercompass.dto.response;

public record AssessmentOptionResponse(
        Long id,
        String optionText,
        int displayOrder
) {
}
