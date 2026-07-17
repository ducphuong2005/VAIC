package com.careercompass.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCrawlRunRequest(
        @NotBlank @Size(max = 80) String sourceName
) {
}
