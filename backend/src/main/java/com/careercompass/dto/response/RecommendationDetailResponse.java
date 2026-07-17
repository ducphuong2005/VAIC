package com.careercompass.dto.response;

import java.util.List;

public record RecommendationDetailResponse(
        Long id,
        String onetCode,
        String careerName,
        String group,
        int rank,
        RecommendationScoreResponse scores,
        double confidence,
        List<String> reasons,
        List<String> considerations,
        List<SkillGapResponse> skillGaps,
        List<String> marketEvidence,
        List<String> sources
) {
}
