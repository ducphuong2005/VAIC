package com.careercompass.recommendation;

import com.careercompass.entity.SkillGap;
import com.careercompass.entity.SkillGapPriority;
import java.math.BigDecimal;
import org.springframework.stereotype.Component;

@Component
public class SkillGapCalculator {

    public SkillGap build(Long recommendationId, String skillName, BigDecimal currentScore, BigDecimal requiredScore) {
        BigDecimal gapScore = requiredScore.subtract(currentScore).max(BigDecimal.ZERO);
        return SkillGap.builder()
                .recommendationId(recommendationId)
                .skillName(skillName)
                .currentScore(currentScore)
                .requiredScore(requiredScore)
                .gapScore(gapScore)
                .priority(priority(gapScore))
                .suggestedCourses("[]")
                .build();
    }

    public SkillGapPriority priority(BigDecimal gapScore) {
        if (gapScore.compareTo(BigDecimal.valueOf(40)) >= 0) {
            return SkillGapPriority.HIGH;
        }
        if (gapScore.compareTo(BigDecimal.valueOf(20)) >= 0) {
            return SkillGapPriority.MEDIUM;
        }
        return SkillGapPriority.LOW;
    }
}
