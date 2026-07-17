package com.careercompass.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careercompass.entity.MiniGameMetric;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MiniGameNormalizationServiceTest {

    private final MiniGameNormalizationService service = new MiniGameNormalizationService();

    @Test
    void normalizesHigherIsBetterMetric() {
        MiniGameMetric metric = metric("0", "1", true);

        assertThat(service.normalize(BigDecimal.valueOf(0.75), metric)).isEqualByComparingTo("75.00");
    }

    @Test
    void reversesLowerIsBetterMetric() {
        MiniGameMetric metric = metric("0", "10", false);

        assertThat(service.normalize(BigDecimal.valueOf(2), metric)).isEqualByComparingTo("80.00");
    }

    private MiniGameMetric metric(String min, String max, boolean higherIsBetter) {
        MiniGameMetric metric = new MiniGameMetric();
        metric.setMinValue(new BigDecimal(min));
        metric.setMaxValue(new BigDecimal(max));
        metric.setHigherIsBetter(higherIsBetter);
        return metric;
    }
}
