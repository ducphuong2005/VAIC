package com.careercompass.llm;

import com.careercompass.entity.LlmPurpose;

public record LlmRequest(
        LlmPurpose purpose,
        String prompt,
        String model
) {
}
