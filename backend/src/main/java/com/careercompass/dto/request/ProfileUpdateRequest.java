package com.careercompass.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.List;

public record ProfileUpdateRequest(
        @Size(max = 50) String educationLevel,
        @Size(max = 255) String school,
        @Size(max = 255) String currentMajor,
        List<@Size(max = 80) String> preferredRegions,
        @DecimalMin("0") BigDecimal learningBudget,
        @Min(0) @Max(168) Integer availableLearningHoursPerWeek,
        List<@Size(max = 80) String> preferredEducationRoutes,
        @Size(max = 2000) String careerGoals,
        List<@Valid ExistingSkillRequest> existingSkills
) {
}
