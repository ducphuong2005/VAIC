package com.careercompass.dto.response;

import java.util.List;

public record LearningPathStepResponse(
        Long id,
        int stepOrder,
        String title,
        String description,
        String targetSkill,
        Long courseId,
        List<LearningResourceResponse> resources,
        Integer durationHours,
        int progressPercent,
        boolean completed
) {
}
