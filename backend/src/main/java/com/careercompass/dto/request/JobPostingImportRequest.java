package com.careercompass.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record JobPostingImportRequest(
        @NotBlank @Size(max = 80) String sourceName,
        @NotBlank @Size(max = 190) String externalId,
        @NotBlank @Size(max = 128) String contentHash,
        @NotBlank @Size(max = 255) String title,
        @Size(max = 255) String companyName,
        @Size(max = 255) String location,
        @Size(max = 120) String region,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        boolean remote,
        boolean entryLevel,
        @Size(max = 20) String onetCode,
        Instant postedAt,
        @Size(max = 1000) String rawUrl,
        @Size(max = 5000) String description,
        List<@Size(max = 190) String> skills
) {
}
