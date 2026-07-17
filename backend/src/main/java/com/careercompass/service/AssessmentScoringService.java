package com.careercompass.service;

import com.careercompass.entity.AssessmentOption;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class AssessmentScoringService {

    private static final double NORMALIZED_MAX = 100.0;
    private final ObjectMapper objectMapper;

    public AssessmentScoringService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Double> score(List<AssessmentOption> selectedOptions) {
        Map<String, Double> rawScores = new LinkedHashMap<>();
        for (AssessmentOption option : selectedOptions) {
            for (Map.Entry<String, Double> entry : parsePayload(option.getScoringPayload()).entrySet()) {
                rawScores.merge(entry.getKey(), entry.getValue(), Double::sum);
            }
        }
        return rawScores;
    }

    public Map<String, Double> normalize(Map<String, Double> rawScores) {
        double max = rawScores.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        Map<String, Double> normalized = new LinkedHashMap<>();
        for (Map.Entry<String, Double> entry : rawScores.entrySet()) {
            double value = max == 0 ? 0 : (entry.getValue() / max) * NORMALIZED_MAX;
            normalized.put(entry.getKey(), Math.round(value * 100.0) / 100.0);
        }
        return normalized;
    }

    private Map<String, Double> parsePayload(String payload) {
        try {
            return objectMapper.readValue(payload, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid assessment scoring payload");
        }
    }
}
