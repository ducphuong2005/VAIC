package com.careercompass.dto.response;

import java.util.UUID;

public record MiniGameSummaryResponse(
        UUID id,
        String code,
        String title,
        String description,
        String gameType,
        int estimatedMinutes
) {
}
