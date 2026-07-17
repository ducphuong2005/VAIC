package com.careercompass.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import com.careercompass.config.RecommendationProperties;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class RecommendationScoringEngineTest {

    @Test
    void appliesConfiguredRecommendationFormula() {
        RecommendationScoringEngine engine = new RecommendationScoringEngine(
                new RecommendationProperties(new RecommendationProperties.Weights(0.25, 0.25, 0.15, 0.10, 0.15, 0.10)),
                null,
                null,
                null,
                null
        );

        BigDecimal score = engine.finalScore(
                BigDecimal.valueOf(90),
                BigDecimal.valueOf(85),
                BigDecimal.valueOf(62),
                BigDecimal.valueOf(78),
                BigDecimal.valueOf(88),
                BigDecimal.valueOf(74)
        );

        assertThat(score).isEqualByComparingTo("81.45");
    }
}
