package com.careercompass.dto.response;

public record TrendingCareerResponse(
        String onetCode,
        String titleVi,
        String titleEn,
        long jobCount,
        String source,
        String limitation
) {
}
