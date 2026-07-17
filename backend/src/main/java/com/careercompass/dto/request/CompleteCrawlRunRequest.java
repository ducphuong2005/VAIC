package com.careercompass.dto.request;

import com.careercompass.entity.CrawlRunStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CompleteCrawlRunRequest(
        @NotNull CrawlRunStatus status,
        @Size(max = 2000) String errorMessage
) {
}
