package com.careercompass.dto.response;

import java.math.BigDecimal;

public record AiCareerOptionResponse(
        String onetCode,
        String titleVi,
        String titleEn,
        BigDecimal score,
        String recommendationGroup,
        String reason
) {
}
