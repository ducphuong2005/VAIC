package com.careercompass.dto.response;

import java.time.Instant;
import java.util.UUID;

public record ChatSessionResponse(
        UUID id,
        String title,
        Instant createdAt,
        Instant updatedAt
) {
}
