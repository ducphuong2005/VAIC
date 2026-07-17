package com.careercompass.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.careercompass.entity.AssessmentOption;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AssessmentScoringServiceTest {

    private final AssessmentScoringService service = new AssessmentScoringService(new ObjectMapper());

    @Test
    void sumsScoringPayloadAndNormalizesToOneHundred() {
        AssessmentOption first = option("{\"RIASEC-I\":8,\"2.B.1.a\":6}");
        AssessmentOption second = option("{\"RIASEC-I\":6,\"1.C.4.b\":5}");

        Map<String, Double> raw = service.score(List.of(first, second));
        Map<String, Double> normalized = service.normalize(raw);

        assertThat(raw).containsEntry("RIASEC-I", 14.0);
        assertThat(normalized).containsEntry("RIASEC-I", 100.0);
        assertThat(normalized).containsEntry("2.B.1.a", 42.86);
    }

    private AssessmentOption option(String payload) {
        AssessmentOption option = new AssessmentOption();
        option.setScoringPayload(payload);
        return option;
    }
}
