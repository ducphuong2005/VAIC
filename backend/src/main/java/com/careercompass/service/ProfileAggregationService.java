package com.careercompass.service;

import com.careercompass.entity.ProfileEvidence;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ProfileAggregationService {

    private static final BigDecimal SINGLE_EVIDENCE_CONFIDENCE_CAP = BigDecimal.valueOf(95);
    private static final BigDecimal MAX_CONFIDENCE = BigDecimal.valueOf(100);

    /**
     * Aggregates dynamic profile dimensions using the required weighted formula:
     * finalScore = SUM(evidenceScore * evidenceConfidence) / SUM(evidenceConfidence).
     * Confidence grows from total evidence confidence and independent evidence count,
     * but a single evidence item is capped at 95 to avoid over-certainty.
     */
    public ProfileAggregationResult aggregate(List<ProfileEvidence> evidenceList) {
        if (evidenceList.isEmpty()) {
            return new ProfileAggregationResult(BigDecimal.ZERO, BigDecimal.ZERO, 0);
        }

        BigDecimal weightedScoreSum = BigDecimal.ZERO;
        BigDecimal confidenceSum = BigDecimal.ZERO;
        for (ProfileEvidence evidence : evidenceList) {
            weightedScoreSum = weightedScoreSum.add(evidence.getEvidenceScore().multiply(evidence.getEvidenceConfidence()));
            confidenceSum = confidenceSum.add(evidence.getEvidenceConfidence());
        }

        BigDecimal score = confidenceSum.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : weightedScoreSum.divide(confidenceSum, 2, RoundingMode.HALF_UP);

        BigDecimal averageConfidence = confidenceSum.divide(BigDecimal.valueOf(evidenceList.size()), 2, RoundingMode.HALF_UP);
        BigDecimal evidenceBonus = BigDecimal.valueOf(Math.max(0, evidenceList.size() - 1) * 5L);
        BigDecimal confidence = averageConfidence.add(evidenceBonus).min(MAX_CONFIDENCE);
        if (evidenceList.size() == 1) {
            confidence = confidence.min(SINGLE_EVIDENCE_CONFIDENCE_CAP);
        }

        return new ProfileAggregationResult(score, confidence, evidenceList.size());
    }
}
