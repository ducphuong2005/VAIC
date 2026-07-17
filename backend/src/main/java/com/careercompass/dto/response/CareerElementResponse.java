package com.careercompass.dto.response;

import java.math.BigDecimal;

public record CareerElementResponse(
        String elementId,
        String name,
        String category,
        BigDecimal score
) {
}
