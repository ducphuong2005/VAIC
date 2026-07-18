package com.careercompass.llm;

import com.careercompass.config.LlmProperties;
import com.careercompass.entity.LlmPurpose;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

@Component
public class OpenAiLlmProvider implements LlmProvider {

    private final LlmProperties properties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public OpenAiLlmProvider(LlmProperties properties, WebClient.Builder builder, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.webClient = builder.baseUrl(properties.baseUrl()).build();
    }

    @Override
    public String providerName() {
        return "openai";
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        if (!StringUtils.hasText(properties.apiKey())) {
            return fallback("Missing LLM_API_KEY");
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", request.model(),
                    "messages", List.of(
                            Map.of(
                                    "role", "system",
                                    "content", "You are Career Compass, a Vietnamese career advisor. Return only valid JSON."
                            ),
                            Map.of("role", "user", "content", request.prompt())
                    ),
                    "response_format", responseFormat(request.purpose())
            );
            String response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                            .filter(this::isRetryableNetworkError)
                            .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                    .block();
            JsonNode root = objectMapper.readTree(response);
            String content = root.path("choices").path(0).path("message").path("content").asText();
            Integer inputTokens = root.path("usage").path("prompt_tokens").isMissingNode()
                    ? null
                    : root.path("usage").path("prompt_tokens").asInt();
            Integer outputTokens = root.path("usage").path("completion_tokens").isMissingNode()
                    ? null
                    : root.path("usage").path("completion_tokens").asInt();
            if (!StringUtils.hasText(content)) {
                return fallback("OpenAI response did not include message content");
            }
            return new LlmResponse(content, inputTokens, outputTokens, true, null);
        } catch (WebClientResponseException exception) {
            return fallback(openAiErrorMessage(exception));
        } catch (RuntimeException exception) {
            return fallback(exception.getMessage());
        } catch (Exception exception) {
            return fallback(exception.getMessage());
        }
    }

    private LlmResponse fallback(String errorMessage) {
        return new LlmResponse(
                "{}",
                null,
                null,
                false,
                normalizeProviderError(errorMessage)
        );
    }

    private boolean isRetryableNetworkError(Throwable exception) {
        String message = exception.getMessage();
        return StringUtils.hasText(message)
                && (message.contains("Failed to resolve")
                || message.contains("Connection reset")
                || message.contains("Connection timed out")
                || message.contains("Read timed out"));
    }

    private String normalizeProviderError(String errorMessage) {
        if (!StringUtils.hasText(errorMessage)) {
            return "Không nhận được lỗi chi tiết từ LLM provider.";
        }
        String safeMessage = sanitize(errorMessage);
        if (safeMessage.contains("Failed to resolve")) {
            return "Không phân giải được api.openai.com. Kiểm tra mạng, DNS, VPN/proxy rồi thử lại.";
        }
        if (safeMessage.contains("Connection timed out") || safeMessage.contains("Read timed out")) {
            return "Kết nối tới OpenAI quá thời gian chờ. Kiểm tra mạng rồi thử lại.";
        }
        return safeMessage;
    }

    private String openAiErrorMessage(WebClientResponseException exception) {
        String message = extractOpenAiErrorMessage(exception.getResponseBodyAsString());
        String status = exception.getStatusCode().value() + " " + exception.getStatusText();
        if (StringUtils.hasText(message)) {
            return "OpenAI API trả " + status + ": " + sanitize(message);
        }
        return "OpenAI API trả " + status;
    }

    private String extractOpenAiErrorMessage(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return "";
        }
        try {
            return objectMapper.readTree(responseBody).path("error").path("message").asText();
        } catch (Exception exception) {
            return responseBody;
        }
    }

    private String sanitize(String message) {
        return message
                .replaceAll("sk-proj-[A-Za-z0-9_-]+", "sk-proj-***")
                .replaceAll("sk-[A-Za-z0-9_-]+", "sk-***");
    }

    private Map<String, Object> responseFormat(LlmPurpose purpose) {
        if (purpose == LlmPurpose.OPEN_RESPONSE_ANALYSIS) {
            return contextAnalysisResponseFormat();
        }
        return answerResponseFormat();
    }

    private Map<String, Object> contextAnalysisResponseFormat() {
        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "career_compass_context_analysis",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "additionalProperties", false,
                                "properties", Map.of(
                                        "intent", Map.of(
                                                "type", "string",
                                                "enum", List.of(
                                                        "PROFILE_DISCOVERY",
                                                        "CAREER_RECOMMENDATION",
                                                        "CAREER_COMPARISON",
                                                        "SKILL_GAP",
                                                        "LEARNING_PATH",
                                                        "MARKET_QUESTION",
                                                        "GENERAL_CAREER_CHAT"
                                                )
                                        ),
                                        "answerMode", Map.of(
                                                "type", "string",
                                                "enum", List.of(
                                                        "SMALL_TALK",
                                                        "CAREER_ADVICE",
                                                        "SKILL_GAP",
                                                        "LEARNING_PATH",
                                                        "MARKET_QUESTION",
                                                        "CAREER_COMPARISON",
                                                        "PROFILE_DISCOVERY",
                                                        "GENERAL"
                                                )
                                        ),
                                        "contextSummary", Map.of("type", "string"),
                                        "ragQuery", Map.of("type", "string"),
                                        "responseGuidance", Map.of("type", "string"),
                                        "shouldUseCareerContext", Map.of("type", "boolean"),
                                        "confidence", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                                        "nextSteps", Map.of(
                                                "type", "array",
                                                "items", Map.of("type", "string")
                                        )
                                ),
                                "required", List.of(
                                        "intent",
                                        "answerMode",
                                        "contextSummary",
                                        "ragQuery",
                                        "responseGuidance",
                                        "shouldUseCareerContext",
                                        "confidence",
                                        "nextSteps"
                                )
                        )
                )
        );
    }

    private Map<String, Object> answerResponseFormat() {
        return Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                        "name", "career_compass_answer",
                        "strict", true,
                        "schema", Map.of(
                                "type", "object",
                                "additionalProperties", false,
                                "properties", Map.of(
                                        "content", Map.of("type", "string"),
                                        "confidence", Map.of("type", "integer", "minimum", 0, "maximum", 100),
                                        "nextSteps", Map.of(
                                                "type", "array",
                                                "items", Map.of("type", "string")
                                        )
                                ),
                                "required", List.of("content", "confidence", "nextSteps")
                        )
                )
        );
    }
}
