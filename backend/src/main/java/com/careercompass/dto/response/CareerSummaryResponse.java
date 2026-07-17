package com.careercompass.dto.response;

public record CareerSummaryResponse(
        String onetCode,
        String titleVi,
        String titleEn,
        String description,
        String careerCluster,
        Integer jobZone
) {
}
