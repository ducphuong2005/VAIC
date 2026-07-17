package com.careercompass.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ExistingSkillRequest(
        @NotBlank String skillName,
        @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal score
) {
}
