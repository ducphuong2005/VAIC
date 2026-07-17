package com.careercompass.dto.response;

import java.util.List;

public record ProfileCompletionResponse(
        int completionPercent,
        List<String> missingFields,
        String status
) {
}
