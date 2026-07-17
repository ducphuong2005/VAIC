package com.careercompass.dto.response;

import java.math.BigDecimal;

public record CareerTaskResponse(
        Long id,
        String taskText,
        BigDecimal importanceScore
) {
}
