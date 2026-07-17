package com.careercompass.dto.response;

import java.time.Instant;

public record FairnessTestRunResponse(Long id, String status, String summary, boolean passed, Instant createdAt) {
}
