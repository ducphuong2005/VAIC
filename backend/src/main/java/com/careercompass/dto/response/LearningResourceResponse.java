package com.careercompass.dto.response;

public record LearningResourceResponse(
        String provider,
        String title,
        String url,
        String targetSkill,
        String reason
) {
}
