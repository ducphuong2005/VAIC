package com.careercompass.llm;

public interface LlmProvider {

    String providerName();

    LlmResponse generate(LlmRequest request);
}
