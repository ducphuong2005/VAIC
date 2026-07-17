package com.careercompass.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MiniGameActionRequest(
        @NotBlank String actionType,
        @NotNull JsonNode actionPayload
) {
}
