package com.careercompass.dto.response;

import java.time.Instant;

public record MiniGameActionResponse(
        Long id,
        String actionType,
        String actionPayload,
        Instant occurredAt
) {
}
