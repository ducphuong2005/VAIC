package com.careercompass.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ProfileResponse(
        UUID userId,
        String educationLevel,
        String school,
        String currentMajor,
        List<String> preferredRegions,
        BigDecimal learningBudget,
        Integer availableLearningHoursPerWeek,
        List<String> preferredEducationRoutes,
        String careerGoals,
        List<ExistingSkillResponse> existingSkills,
        Instant updatedAt
) {
}
