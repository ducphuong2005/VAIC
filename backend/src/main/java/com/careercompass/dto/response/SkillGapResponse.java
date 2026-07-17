package com.careercompass.dto.response;

import java.util.List;

public record SkillGapResponse(
        Long id,
        String skillName,
        double currentScore,
        double requiredScore,
        double gapScore,
        String priority,
        List<String> suggestedCourses
) {
}
