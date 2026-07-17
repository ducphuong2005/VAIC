package com.careercompass.llm;

import org.springframework.stereotype.Component;

@Component
public class GeminiLlmProvider implements LlmProvider {

    @Override
    public String providerName() {
        return "gemini";
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        return new LlmResponse("{\"content\":\"Gemini provider placeholder response\",\"confidence\":60}", null, null, true, null);
    }
}
