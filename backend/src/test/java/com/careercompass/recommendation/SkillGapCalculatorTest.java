package com.careercompass.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import com.careercompass.entity.SkillGap;
import com.careercompass.entity.SkillGapPriority;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class SkillGapCalculatorTest {

    private final SkillGapCalculator calculator = new SkillGapCalculator();

    @Test
    void calculatesHighMediumAndLowPriorities() {
        assertThat(calculator.priority(BigDecimal.valueOf(40))).isEqualTo(SkillGapPriority.HIGH);
        assertThat(calculator.priority(BigDecimal.valueOf(20))).isEqualTo(SkillGapPriority.MEDIUM);
        assertThat(calculator.priority(BigDecimal.valueOf(19))).isEqualTo(SkillGapPriority.LOW);
    }

    @Test
    void gapScoreNeverGoesBelowZero() {
        SkillGap gap = calculator.build(1L, "SQL", BigDecimal.valueOf(90), BigDecimal.valueOf(70));

        assertThat(gap.getGapScore()).isEqualByComparingTo("0");
        assertThat(gap.getPriority()).isEqualTo(SkillGapPriority.LOW);
    }
}
