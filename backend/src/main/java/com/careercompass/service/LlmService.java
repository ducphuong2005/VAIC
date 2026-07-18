package com.careercompass.service;

import com.careercompass.config.LlmProperties;
import com.careercompass.entity.LlmCall;
import com.careercompass.entity.LlmPurpose;
import com.careercompass.exception.ApiException;
import com.careercompass.llm.LlmProvider;
import com.careercompass.llm.LlmRequest;
import com.careercompass.llm.LlmResponse;
import com.careercompass.repository.LlmCallRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class LlmService {

    private final List<LlmProvider> providers;
    private final LlmProperties properties;
    private final LlmCallRepository llmCallRepository;

    public LlmResponse generate(LlmPurpose purpose, String prompt) {
        String providerName = StringUtils.hasText(properties.provider()) ? properties.provider() : "gemini";
        LlmProvider provider = providers.stream()
                .filter(candidate -> candidate.providerName().equalsIgnoreCase(providerName))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "LLM provider chưa được cấu hình: " + providerName));
        Instant start = Instant.now();
        String model = modelFor(purpose);
        LlmResponse response = provider.generate(new LlmRequest(purpose, prompt, model));
        long latency = Duration.between(start, Instant.now()).toMillis();
        llmCallRepository.save(LlmCall.builder()
                .purpose(purpose)
                .provider(provider.providerName())
                .model(model)
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

    private String modelFor(LlmPurpose purpose) {
        if (purpose == LlmPurpose.OPEN_RESPONSE_ANALYSIS && StringUtils.hasText(properties.contextModel())) {
            return properties.contextModel();
        }
        if ((purpose == LlmPurpose.CHAT_RESPONSE || purpose == LlmPurpose.RECOMMENDATION_EXPLANATION)
                && StringUtils.hasText(properties.answerModel())) {
            return properties.answerModel();
        }
        return StringUtils.hasText(properties.model()) ? properties.model() : defaultModel();
    }

    private String defaultModel() {
        if (!StringUtils.hasText(properties.provider()) || "gemini".equalsIgnoreCase(properties.provider())) {
            return "gemini-flash-lite-latest";
        }
        return "gpt-4o-mini";
    }
}
