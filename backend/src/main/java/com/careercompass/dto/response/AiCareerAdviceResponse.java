package com.careercompass.dto.response;

import java.util.List;

public record AiCareerAdviceResponse(
        String content,
        int confidence,
        List<String> topRiasecTypes,
        List<AiCareerOptionResponse> careerOptions,
        List<JobPostingResponse> marketJobs,
        List<String> nextSteps,
        List<String> sources
) {
}
