package com.careercompass.llm;

import com.careercompass.config.LlmProperties;
import com.careercompass.entity.LlmPurpose;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

@Component
public class GeminiLlmProvider implements LlmProvider {

    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final String SYSTEM_INSTRUCTION = "You are Career Compass, a Vietnamese career advisor. "
            + "Return only valid JSON that matches the requested schema.";

    private final LlmProperties properties;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public GeminiLlmProvider(LlmProperties properties, WebClient.Builder builder, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.webClient = builder.baseUrl(baseUrl()).build();
    }

    @Override
    public String providerName() {
        return "gemini";
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        String apiKey = apiKey();
        if (!StringUtils.hasText(apiKey) || isPlaceholderKey(apiKey)) {
            return fallback("Missing GEMINI_API_KEY");
        }
        try {
            Map<String, Object> body = Map.of(
                    "system_instruction", Map.of(
                            "parts", List.of(Map.of("text", SYSTEM_INSTRUCTION))
                    ),
                    "contents", List.of(Map.of(
                            "role", "user",
                            "parts", List.of(Map.of("text", request.prompt()))
                    )),
                    "generationConfig", Map.of(
                            "response_mime_type", "application/json",
                            "response_schema", responseSchema(request.purpose()),
                            "temperature", 0.2
                    )
            );
            String response = webClient.post()
                    .uri("/models/{model}:generateContent", normalizedModel(request.model()))
                    .header("x-goog-api-key", apiKey)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
                    .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                            .filter(this::isRetryableNetworkError)
                            .onRetryExhaustedThrow((spec, signal) -> signal.failure()))
                    .block();

            JsonNode root = objectMapper.readTree(response);
            String content = extractText(root);
            Integer inputTokens = intOrNull(root.path("usageMetadata").path("promptTokenCount"));
            Integer outputTokens = intOrNull(root.path("usageMetadata").path("candidatesTokenCount"));
            if (!StringUtils.hasText(content)) {
                return fallback(emptyResponseMessage(root));
            }
            return new LlmResponse(content, inputTokens, outputTokens, true, null);
        } catch (WebClientResponseException exception) {
            return fallback(geminiErrorMessage(exception));
        } catch (RuntimeException exception) {
            return fallback(exception.getMessage());
        } catch (Exception exception) {
            return fallback(exception.getMessage());
        }
    }

    private String baseUrl() {
        return StringUtils.hasText(properties.geminiBaseUrl()) ? properties.geminiBaseUrl() : DEFAULT_BASE_URL;
    }

    private String apiKey() {
        return StringUtils.hasText(properties.geminiApiKey()) ? properties.geminiApiKey() : properties.apiKey();
    }

    private boolean isPlaceholderKey(String apiKey) {
        return "replace-with-your-gemini-api-key".equalsIgnoreCase(apiKey.trim())
                || "change-me".equalsIgnoreCase(apiKey.trim());
    }

    private String normalizedModel(String model) {
        String configuredModel = StringUtils.hasText(model) ? model.trim() : "gemini-flash-lite-latest";
        return configuredModel.startsWith("models/") ? configuredModel.substring("models/".length()) : configuredModel;
    }

    private LlmResponse fallback(String errorMessage) {
        return new LlmResponse("{}", null, null, false, normalizeProviderError(errorMessage));
    }

    private String extractText(JsonNode root) {
        return root.path("candidates")
                .path(0)
                .path("content")
                .path("parts")
                .path(0)
                .path("text")
                .asText();
    }

    private Integer intOrNull(JsonNode node) {
        return node.isMissingNode() || node.isNull() ? null : node.asInt();
    }

    private String emptyResponseMessage(JsonNode root) {
        String finishReason = root.path("candidates").path(0).path("finishReason").asText();
        if (StringUtils.hasText(finishReason)) {
            return "Gemini response did not include text content. finishReason=" + finishReason;
        }
        String blockReason = root.path("promptFeedback").path("blockReason").asText();
        if (StringUtils.hasText(blockReason)) {
            return "Gemini blocked the prompt. blockReason=" + blockReason;
        }
        return "Gemini response did not include text content";
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
            return "Không nhận được lỗi chi tiết từ Gemini.";
        }
        String safeMessage = sanitize(errorMessage);
        String normalized = safeMessage.toLowerCase(Locale.ROOT);
        if (normalized.contains("too many requests") || normalized.contains("quota")) {
            return "Gemini API đang hết quota/rate limit cho model hiện tại. Hãy dùng model còn quota hoặc key/project đã bật billing.";
        }
        if (normalized.contains("model") && normalized.contains("no longer available")) {
            return "Model Gemini hiện tại không còn khả dụng cho project này. Hãy đổi LLM_MODEL/LLM_CONTEXT_MODEL/LLM_ANSWER_MODEL sang model khác.";
        }
        if (safeMessage.contains("Failed to resolve")) {
            return "Không phân giải được generativelanguage.googleapis.com. Kiểm tra mạng, DNS, VPN/proxy rồi thử lại.";
        }
        if (safeMessage.contains("Connection timed out") || safeMessage.contains("Read timed out")) {
            return "Kết nối tới Gemini quá thời gian chờ. Kiểm tra mạng rồi thử lại.";
        }
        return safeMessage;
    }

    private String geminiErrorMessage(WebClientResponseException exception) {
        String message = extractGeminiErrorMessage(exception.getResponseBodyAsString());
        String status = exception.getStatusCode().value() + " " + exception.getStatusText();
        if (StringUtils.hasText(message)) {
            return "Gemini API trả " + status + ": " + sanitize(message);
        }
        return "Gemini API trả " + status;
    }

    private String extractGeminiErrorMessage(String responseBody) {
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
                .replaceAll("AIza[0-9A-Za-z_-]+", "AIza***")
                .replaceAll("sk-proj-[A-Za-z0-9_-]+", "sk-proj-***")
                .replaceAll("sk-or-v1-[A-Za-z0-9_-]+", "sk-or-v1-***")
                .replaceAll("sk-[A-Za-z0-9_-]+", "sk-***");
    }

    private Map<String, Object> responseSchema(LlmPurpose purpose) {
        if (purpose == LlmPurpose.OPEN_RESPONSE_ANALYSIS) {
            return contextAnalysisSchema();
        }
        return answerSchema();
    }

    private Map<String, Object> contextAnalysisSchema() {
        return objectSchema(
                Map.of(
                        "intent", stringEnumSchema(List.of(
                                "PROFILE_DISCOVERY",
                                "CAREER_RECOMMENDATION",
                                "CAREER_COMPARISON",
                                "SKILL_GAP",
                                "LEARNING_PATH",
                                "MARKET_QUESTION",
                                "GENERAL_CAREER_CHAT"
                        )),
                        "answerMode", stringEnumSchema(List.of(
                                "SMALL_TALK",
                                "CAREER_ADVICE",
                                "SKILL_GAP",
                                "LEARNING_PATH",
                                "MARKET_QUESTION",
                                "CAREER_COMPARISON",
                                "PROFILE_DISCOVERY",
                                "GENERAL"
                        )),
                        "contextSummary", stringSchema(),
                        "ragQuery", stringSchema(),
                        "responseGuidance", stringSchema(),
                        "shouldUseCareerContext", booleanSchema(),
                        "confidence", integerSchema(0, 100),
                        "nextSteps", arraySchema(stringSchema())
                ),
                List.of(
                        "intent",
                        "answerMode",
                        "contextSummary",
                        "ragQuery",
                        "responseGuidance",
                        "shouldUseCareerContext",
                        "confidence",
                        "nextSteps"
                )
        );
    }

    private Map<String, Object> answerSchema() {
        return objectSchema(
                Map.of(
                        "content", stringSchema(),
                        "confidence", integerSchema(0, 100),
                        "nextSteps", arraySchema(stringSchema())
                ),
                List.of("content", "confidence", "nextSteps")
        );
    }

    private Map<String, Object> objectSchema(Map<String, Object> properties, List<String> required) {
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("type", "OBJECT");
        schema.put("properties", properties);
        schema.put("required", required);
        return schema;
    }

    private Map<String, Object> stringSchema() {
        return Map.of("type", "STRING");
    }

    private Map<String, Object> stringEnumSchema(List<String> values) {
        return Map.of("type", "STRING", "enum", values);
    }

    private Map<String, Object> booleanSchema() {
        return Map.of("type", "BOOLEAN");
    }

    private Map<String, Object> integerSchema(int minimum, int maximum) {
        return Map.of("type", "INTEGER", "minimum", minimum, "maximum", maximum);
    }

    private Map<String, Object> arraySchema(Map<String, Object> itemSchema) {
        return Map.of("type", "ARRAY", "items", itemSchema);
    }
}
