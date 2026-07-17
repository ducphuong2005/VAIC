package com.careercompass.service;

import com.careercompass.entity.MiniGameMetric;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class MiniGameNormalizationService {

    public BigDecimal normalize(BigDecimal rawValue, MiniGameMetric metric) {
        BigDecimal range = metric.getMaxValue().subtract(metric.getMinValue());
        if (range.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal bounded = rawValue.max(metric.getMinValue()).min(metric.getMaxValue());
        BigDecimal ratio = bounded.subtract(metric.getMinValue()).divide(range, 6, RoundingMode.HALF_UP);
        if (!metric.isHigherIsBetter()) {
            ratio = BigDecimal.ONE.subtract(ratio);
        }
        return ratio.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }
}
