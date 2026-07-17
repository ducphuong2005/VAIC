package com.careercompass.recommendation;

import com.careercompass.entity.Occupation;
import com.careercompass.entity.RecommendationGroup;
import java.math.BigDecimal;
import java.util.List;

public record RecommendationCandidate(
        Occupation occupation,
        RecommendationGroup group,
        BigDecimal interestScore,
        BigDecimal abilityScore,
        BigDecimal skillScore,
        BigDecimal workStyleScore,
        BigDecimal marketScore,
        BigDecimal feasibilityScore,
        BigDecimal finalScore,
        BigDecimal confidence,
        List<String> reasons,
        List<String> considerations,
        List<String> sources
) {
}
