package com.careercompass.llm;

import com.careercompass.entity.LlmPurpose;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MockLlmProvider implements LlmProvider {

    private final ObjectMapper objectMapper;

    @Override
    public String providerName() {
        return "mock";
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        try {
            JsonNode payload = payloadFrom(request.prompt());
            String content = request.purpose() == LlmPurpose.OPEN_RESPONSE_ANALYSIS
                    ? contextAnalysis(payload)
                    : answer(request.purpose(), payload);
            return new LlmResponse(content, 50, 40, true, null);
        } catch (Exception exception) {
            return new LlmResponse("{}", null, null, false, exception.getMessage());
        }
    }

    private JsonNode payloadFrom(String prompt) throws Exception {
        int index = prompt.lastIndexOf("Payload:");
        if (index < 0) {
            return objectMapper.createObjectNode();
        }
        return objectMapper.readTree(prompt.substring(index + "Payload:".length()).trim());
    }

    private String contextAnalysis(JsonNode payload) throws Exception {
        String message = payload.path("latestUserMessage").asText("").toLowerCase(Locale.ROOT);
        boolean careerContext = containsAny(message, "nghề", "ngành", "skill", "kỹ năng", "học", "lương", "thị trường", "dữ liệu", "data");
        String intent = careerContext ? "CAREER_RECOMMENDATION" : "GENERAL_CAREER_CHAT";
        if (containsAny(message, "thiếu", "skill", "kỹ năng")) {
            intent = "SKILL_GAP";
        } else if (containsAny(message, "lộ trình", "học")) {
            intent = "LEARNING_PATH";
        } else if (containsAny(message, "lương", "thị trường")) {
            intent = "MARKET_QUESTION";
        } else if (containsAny(message, "so sánh")) {
            intent = "CAREER_COMPARISON";
        }
        return objectMapper.writeValueAsString(Map.of(
                "intent", intent,
                "answerMode", careerContext ? "CAREER_ADVICE" : "SMALL_TALK",
                "contextSummary", "Đã đọc tin nhắn mới, lịch sử hội thoại và careerContext hiện có.",
                "ragQuery", String.join(" ", message, "RIASEC job TopCV").trim(),
                "responseGuidance", careerContext
                        ? "Trả lời theo hướng tư vấn nghề, liên hệ RIASEC/careerContext nếu có dữ liệu."
                        : "Trả lời tự nhiên với lời chào ngắn và gợi ý người dùng hỏi tiếp về hướng nghề.",
                "shouldUseCareerContext", careerContext,
                "confidence", careerContext ? 76 : 88,
                "nextSteps", List.of("Đọc careerContext", "Trả lời đúng intent", "Không bịa nguồn ngoài payload")
        ));
    }

    private String answer(LlmPurpose purpose, JsonNode payload) throws Exception {
        String message = payload.path("latestUserMessage").asText();
        String mode = payload.path("contextAnalysisFromLlm1").path("answerMode").asText();
        String content;
        if ("SMALL_TALK".equals(mode)) {
            content = "Mình đã đọc lời nhắn của bạn. Bạn muốn mình hỗ trợ phân tích hướng nghề, kỹ năng cần học, RIASEC hay job thị trường trước?";
        } else if (purpose == LlmPurpose.RECOMMENDATION_EXPLANATION) {
            content = "Mình đã đọc dữ liệu hồ sơ, RIASEC, nghề gợi ý và job market trong payload để tạo tư vấn hướng nghiệp phù hợp.";
        } else {
            content = "Mình đã đọc câu hỏi \"" + message + "\" cùng context nghề nghiệp hiện có. Dựa trên dữ liệu đó, mình sẽ ưu tiên phân tích các hướng liên quan tới dữ liệu, kỹ năng cần bổ sung và tín hiệu job market trong payload.";
        }
        return objectMapper.writeValueAsString(Map.of(
                "content", content,
                "confidence", "SMALL_TALK".equals(mode) ? 88 : 76,
                "nextSteps", List.of(
                        "Hoàn thành thêm khảo sát hoặc mini-game để tăng độ tin cậy.",
                        "Hỏi tiếp về kỹ năng, lộ trình học hoặc job cụ thể bạn muốn so sánh."
                ),
                "learningResources", List.of(
                        Map.of(
                                "provider", "W3Schools",
                                "title", "SQL Tutorial",
                                "url", "https://www.w3schools.com/sql/",
                                "targetSkill", "SQL",
                                "reason", "Rèn nền tảng truy vấn dữ liệu qua ví dụ thực hành."
                        ),
                        Map.of(
                                "provider", "Coursera",
                                "title", "Tìm khóa học Data Analysis trên Coursera",
                                "url", "https://www.coursera.org/courses?query=data%20analysis",
                                "targetSkill", "Data Analysis",
                                "reason", "Chọn khóa học/certificate phù hợp với hướng phân tích dữ liệu."
                        )
                )
        ));
    }

    private boolean containsAny(String text, String... needles) {
        for (String needle : needles) {
            if (text.contains(needle)) {
                return true;
            }
        }
        return false;
    }
}
