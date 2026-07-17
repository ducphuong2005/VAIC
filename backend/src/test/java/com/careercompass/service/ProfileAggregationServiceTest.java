package com.careercompass.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careercompass.entity.EvidenceSourceType;
import com.careercompass.entity.ProfileEvidence;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProfileAggregationServiceTest {

    private final ProfileAggregationService service = new ProfileAggregationService();

    @Test
    void calculatesWeightedScoreByEvidenceConfidence() {
        ProfileAggregationResult result = service.aggregate(List.of(
                evidence("RIASEC-I", 80, 50),
                evidence("RIASEC-I", 100, 100)
        ));

        assertThat(result.score()).isEqualByComparingTo("93.33");
        assertThat(result.confidence()).isEqualByComparingTo("80.00");
        assertThat(result.evidenceCount()).isEqualTo(2);
    }

    @Test
    void capsConfidenceWhenOnlyOneEvidenceExists() {
        ProfileAggregationResult result = service.aggregate(List.of(evidence("RIASEC-I", 88, 99)));

        assertThat(result.score()).isEqualByComparingTo("88.00");
        assertThat(result.confidence()).isEqualByComparingTo("95.00");
    }

    private ProfileEvidence evidence(String elementId, int score, int confidence) {
        return ProfileEvidence.builder()
                .userId(UUID.randomUUID())
                .sourceType(EvidenceSourceType.ASSESSMENT)
                .elementId(elementId)
                .evidenceScore(BigDecimal.valueOf(score))
                .evidenceConfidence(BigDecimal.valueOf(confidence))
                .build();
    }
}
