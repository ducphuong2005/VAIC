package com.careercompass.dto.response;

import java.time.Instant;

public record ActivityLogResponse(Long id, String activityType, String message, String metadata, Instant createdAt) {
}
