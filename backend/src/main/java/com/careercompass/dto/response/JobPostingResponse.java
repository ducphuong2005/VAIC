package com.careercompass.dto.response;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record JobPostingResponse(
        Long id,
        String sourceName,
        String externalId,
        String title,
        String companyName,
        String location,
        String region,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        boolean remote,
        boolean entryLevel,
        String onetCode,
        Instant postedAt,
        Instant crawledAt,
        String rawUrl,
        List<String> skills
) {
}
