package com.careercompass.service;

import com.careercompass.config.LlmProperties;
import com.careercompass.entity.LlmCall;
import com.careercompass.entity.LlmPurpose;
import com.careercompass.llm.LlmProvider;
import com.careercompass.llm.LlmRequest;
import com.careercompass.llm.LlmResponse;
import com.careercompass.repository.LlmCallRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LlmService {

    private final List<LlmProvider> providers;
    private final LlmProperties properties;
    private final LlmCallRepository llmCallRepository;

    public LlmResponse generate(LlmPurpose purpose, String prompt) {
        LlmProvider provider = providers.stream()
                .filter(candidate -> candidate.providerName().equalsIgnoreCase(properties.provider()))
                .findFirst()
                .orElseGet(() -> providers.stream()
                        .filter(candidate -> candidate.providerName().equalsIgnoreCase("mock"))
                        .findFirst()
                        .orElseThrow());
        Instant start = Instant.now();
        LlmResponse response = provider.generate(new LlmRequest(purpose, prompt, properties.model()));
        long latency = Duration.between(start, Instant.now()).toMillis();
        llmCallRepository.save(LlmCall.builder()
                .purpose(purpose)
                .provider(provider.providerName())
                .model(properties.model())
                .latencyMs(latency)
                .inputTokens(response.inputTokens())
                .outputTokens(response.outputTokens())
                .success(response.success())
                .errorMessage(response.errorMessage())
                .inputPayload(prompt)
                .outputPayload(response.content())
                .build());
        return response;
    }
}
