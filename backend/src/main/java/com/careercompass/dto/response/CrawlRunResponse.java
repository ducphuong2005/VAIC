package com.careercompass.dto.response;

import java.time.Instant;

public record CrawlRunResponse(
        Long id,
        String sourceName,
        String status,
        Instant startedAt,
        Instant finishedAt,
        int inserted,
        int updated,
        int duplicate,
        int rejected,
        String errorMessage
) {
}
