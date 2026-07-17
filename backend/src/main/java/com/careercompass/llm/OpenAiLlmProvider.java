package com.careercompass.llm;

import com.careercompass.config.LlmProperties;
import java.time.Duration;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class OpenAiLlmProvider implements LlmProvider {

    private final LlmProperties properties;
    private final WebClient webClient;

    public OpenAiLlmProvider(LlmProperties properties, WebClient.Builder builder) {
        this.properties = properties;
        this.webClient = builder.baseUrl(properties.baseUrl()).build();
    }

    @Override
    public String providerName() {
        return "openai";
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        try {
            String response = webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .bodyValue("""
                            {"model":"%s","messages":[{"role":"user","content":%s}],"response_format":{"type":"json_object"}}
                            """.formatted(request.model(), quote(request.prompt())))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
                    .block();
            return new LlmResponse(response, null, null, true, null);
        } catch (RuntimeException exception) {
            return new LlmResponse("{\"content\":\"Fallback response\",\"confidence\":50}", null, null, false, exception.getMessage());
        }
    }

    private String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n") + "\"";
    }
}
