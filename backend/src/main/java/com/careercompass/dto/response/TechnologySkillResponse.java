package com.careercompass.dto.response;

import java.math.BigDecimal;

public record TechnologySkillResponse(
        Long id,
        String skillName,
        String category,
        BigDecimal requiredScore
) {
}
