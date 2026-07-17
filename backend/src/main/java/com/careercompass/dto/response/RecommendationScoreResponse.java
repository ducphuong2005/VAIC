package com.careercompass.dto.response;

public record RecommendationScoreResponse(
        double interest,
        double ability,
        double skill,
        double workStyle,
        double market,
        double feasibility,
        double finalScore
) {
}
