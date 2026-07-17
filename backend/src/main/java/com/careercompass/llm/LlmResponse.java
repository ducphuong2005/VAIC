package com.careercompass.llm;

public record LlmResponse(
        String content,
        Integer inputTokens,
        Integer outputTokens,
        boolean success,
        String errorMessage
) {
}
