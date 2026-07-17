package com.careercompass.dto.response;

import java.util.List;
import java.util.UUID;

public record MiniGameDetailResponse(
        UUID id,
        String code,
        String title,
        String description,
        String gameType,
        int estimatedMinutes,
        List<MiniGameMetricResponse> metrics
) {
}
