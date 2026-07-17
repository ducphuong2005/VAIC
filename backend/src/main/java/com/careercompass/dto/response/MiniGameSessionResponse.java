package com.careercompass.dto.response;

import java.time.Instant;
import java.util.UUID;

public record MiniGameSessionResponse(
        UUID sessionId,
        UUID miniGameId,
        String status,
        Instant startedAt,
        Instant completedAt
) {
}
