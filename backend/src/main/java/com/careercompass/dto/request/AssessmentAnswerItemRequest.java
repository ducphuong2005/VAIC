package com.careercompass.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssessmentAnswerItemRequest(
        @NotNull Long questionId,
        @NotNull Long optionId
) {
}
