package com.careercompass.dto.response;

import java.util.List;
import java.util.UUID;

public record LearningPathResponse(UUID id, String onetCode, String title, String route, String status, List<LearningPathStepResponse> steps) {
}
