package com.careercompass.dto.response;

import java.math.BigDecimal;

public record ExistingSkillResponse(
        Long id,
        String skillName,
        BigDecimal score
) {
}
