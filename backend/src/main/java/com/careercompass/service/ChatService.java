package com.careercompass.service;

import com.careercompass.dto.request.CreateChatSessionRequest;
import com.careercompass.dto.request.SendChatMessageRequest;
import com.careercompass.dto.response.ChatMessageResponse;
import com.careercompass.dto.response.ChatSessionDetailResponse;
import com.careercompass.dto.response.ChatSessionResponse;
import com.careercompass.dto.response.ConversationMessageResponse;
import com.careercompass.dto.response.LearningResourceResponse;
import com.careercompass.entity.ChatIntent;
import com.careercompass.entity.ConversationMessage;
import com.careercompass.entity.ConversationSession;
import com.careercompass.entity.LlmPurpose;
import com.careercompass.entity.MessageSender;
import com.careercompass.entity.RagRetrievalLog;
import com.careercompass.exception.ApiException;
import com.careercompass.llm.LlmResponse;
import com.careercompass.rag.RagDocumentResult;
import com.careercompass.rag.RagQuery;
import com.careercompass.rag.RagRetriever;
import com.careercompass.repository.ConversationMessageRepository;
import com.careercompass.repository.ConversationSessionRepository;
import com.careercompass.repository.RagRetrievalLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationSessionRepository sessionRepository;
    private final ConversationMessageRepository messageRepository;
    private final RagRetrievalLogRepository retrievalLogRepository;
    private final RagRetriever ragRetriever;
    private final LlmService llmService;
    private final AiCareerAdviceService aiCareerAdviceService;
    private final LearningResourceService learningResourceService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ChatSessionResponse create(UUID userId, CreateChatSessionRequest request) {
        ConversationSession session = sessionRepository.save(ConversationSession.builder()
                .userId(userId)
                .title(request.title() == null ? "Career chat" : request.title())
                .build());
        return toSessionResponse(session);
    }

    @Transactional(readOnly = true)
    public List<ChatSessionResponse> sessions(UUID userId) {
        return sessionRepository.findByUserIdOrderByUpdatedAtDesc(userId).stream().map(this::toSessionResponse).toList();
    }

    @Transactional(readOnly = true)
    public ChatSessionDetailResponse detail(UUID userId, UUID sessionId) {
        ConversationSession session = getOwnedSession(userId, sessionId);
        return new ChatSessionDetailResponse(
                session.getId(),
                session.getTitle(),
                messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream().map(this::toMessageResponse).toList()
        );
    }

    @Transactional
    public ChatMessageResponse message(UUID userId, UUID sessionId, SendChatMessageRequest request) {
        ConversationSession session = getOwnedSession(userId, sessionId);
        ConversationMessage userMessage = messageRepository.save(ConversationMessage.builder()
                .sessionId(session.getId())
                .sender(MessageSender.USER)
                .content(request.content())
                .build());

        AiCareerAdviceService.CareerContext careerContext = aiCareerAdviceService.buildCareerContext(
                userId,
                "CHAT",
                request.content()
        );
        List<ConversationMessageResponse> recentMessages = recentMessages(session.getId());
        LlmResponse contextResponse = llmService.generate(
                LlmPurpose.OPEN_RESPONSE_ANALYSIS,
                buildContextAnalysisPrompt(request.content(), recentMessages, careerContext)
        );
        ContextAnalysis contextAnalysis = parseContextAnalysis(requireSuccessfulLlm(contextResponse, "LLM đọc context"));
        userMessage.setIntent(contextAnalysis.intent());
        messageRepository.save(userMessage);

        List<RagDocumentResult> documents = contextAnalysis.shouldUseCareerContext()
                ? ragRetriever.retrieve(new RagQuery(contextAnalysis.ragQuery(), null, "vi", careerContext.preferredRegion(), null, null, 5))
                : List.of();
        List<String> sources = documents.stream().map(RagDocumentResult::title).toList();
        retrievalLogRepository.save(RagRetrievalLog.builder()
                .userId(userId)
                .queryText(contextAnalysis.ragQuery())
                .filtersPayload(toJson(Map.of("intent", contextAnalysis.intent().name(), "answerMode", contextAnalysis.answerMode())))
                .resultsPayload(toJson(sources))
                .build());

        LlmResponse answerResponse = llmService.generate(
                LlmPurpose.CHAT_RESPONSE,
                buildAnswerPrompt(request.content(), recentMessages, careerContext, contextAnalysis, documents)
        );
        ParsedAssistantResponse parsed = parseAssistantResponse(requireSuccessfulLlm(answerResponse, "LLM trả lời"));
        ConversationMessage assistant = messageRepository.save(ConversationMessage.builder()
                .sessionId(session.getId())
                .sender(MessageSender.ASSISTANT)
                .content(parsed.content())
                .intent(contextAnalysis.intent())
                .confidence(BigDecimal.valueOf(parsed.confidence()))
                .sources(toJson(sources))
                .build());
        session.setTitle(session.getTitle() == null ? "Career chat" : session.getTitle());
        session.setUpdatedAt(Instant.now());
        sessionRepository.save(session);
        if (shouldPublishGuidance(contextAnalysis, careerContext)) {
            aiCareerAdviceService.publishCareerGuidance(
                    userId,
                    careerContext,
                    "AI_CHAT_GUIDANCE_GENERATED",
                    "AI guidance generated from chatbot and data/jobs.csv",
                    parsed.content(),
                    parsed.confidence(),
                    parsed.nextSteps(),
                    parsed.learningResources()
            );
        }
        return new ChatMessageResponse(
                assistant.getId(),
                parsed.content(),
                contextAnalysis.intent().name(),
                List.of(),
                List.<Object>of(Map.of(
                        "contextAnalysis", contextAnalysisPayload(contextAnalysis),
                        "careerContext", careerContext,
                        "nextSteps", parsed.nextSteps(),
                        "learningResources", parsed.learningResources()
                )),
                sources,
                parsed.confidence()
        );
    }

    private boolean shouldPublishGuidance(ContextAnalysis contextAnalysis, AiCareerAdviceService.CareerContext careerContext) {
        if (careerContext.careerOptions().isEmpty()) {
            return false;
        }
        return switch (contextAnalysis.intent()) {
            case CAREER_RECOMMENDATION, CAREER_COMPARISON, SKILL_GAP, LEARNING_PATH, MARKET_QUESTION, PROFILE_DISCOVERY -> true;
            case GENERAL_CAREER_CHAT -> contextAnalysis.shouldUseCareerContext();
        };
    }

    private List<ConversationMessageResponse> recentMessages(UUID sessionId) {
        List<ConversationMessageResponse> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).stream()
                .map(this::toMessageResponse)
                .toList();
        int from = Math.max(0, messages.size() - 8);
        return messages.subList(from, messages.size());
    }

    private String buildContextAnalysisPrompt(
            String message,
            List<ConversationMessageResponse> recentMessages,
            AiCareerAdviceService.CareerContext careerContext
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("latestUserMessage", message);
        payload.put("recentConversation", recentMessages);
        payload.put("careerContext", careerContext);
        payload.put("allowedIntents", java.util.Arrays.stream(ChatIntent.values()).map(Enum::name).toList());
        return """
                Bạn là LLM 1 trong pipeline Career Compass.
                Nhiệm vụ: đọc tin nhắn mới, lịch sử hội thoại, dữ liệu hồ sơ, RIASEC, career options và job market context.
                Không trả lời người dùng ở bước này. Chỉ định hướng cho LLM 2.
                Nếu người dùng chỉ chào/xã giao, vẫn phải phân tích và hướng dẫn LLM 2 trả lời tự nhiên.
                Nếu cần dữ liệu RAG/job/RIASEC, đặt shouldUseCareerContext=true và viết ragQuery bằng tiếng Việt/ngắn gọn.
                Return JSON object exactly:
                {
                  "intent": "một trong allowedIntents",
                  "answerMode": "SMALL_TALK | CAREER_ADVICE | SKILL_GAP | LEARNING_PATH | MARKET_QUESTION | CAREER_COMPARISON | PROFILE_DISCOVERY | GENERAL",
                  "contextSummary": "tóm tắt context quan trọng cho LLM 2",
                  "ragQuery": "truy vấn RAG phù hợp, hoặc chính tin nhắn người dùng nếu không cần tra sâu",
                  "responseGuidance": "chiến lược trả lời cụ thể cho LLM 2",
                  "shouldUseCareerContext": true,
                  "confidence": 0-100,
                  "nextSteps": ["2-4 gợi ý ngắn cho LLM 2 nếu cần"]
                }
                Payload:
                %s
                """.formatted(toJson(payload));
    }

    private String buildAnswerPrompt(
            String message,
            List<ConversationMessageResponse> recentMessages,
            AiCareerAdviceService.CareerContext careerContext,
            ContextAnalysis contextAnalysis,
            List<RagDocumentResult> documents
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("latestUserMessage", message);
        payload.put("recentConversation", recentMessages);
        payload.put("contextAnalysisFromLlm1", contextAnalysisPayload(contextAnalysis));
        payload.put("careerContext", careerContext);
        payload.put("ragDocuments", documents.stream()
                .map(doc -> doc.title() + ": " + doc.content())
                .toList());
        return """
                Bạn là LLM 2 trong pipeline Career Compass.
                Nhiệm vụ: dựa trên định hướng của LLM 1 để trả lời trực tiếp cho người dùng bằng tiếng Việt.
                Không bịa job, công ty, lương hoặc nguồn tuyển dụng ngoài payload.
                Khi dùng dữ liệu job, nói rõ đó là mẫu crawl TopCV/context hiện có.
                Khi tư vấn nghề, phải dựa trên RIASEC và dữ liệu careerContext/ragDocuments nếu có.
                Giọng trả lời tự nhiên, hỗ trợ, tránh kết luận tuyệt đối.
                Trả lời ngắn gọn trong 2-4 câu. Không dùng Markdown, bullet, bảng, tiêu đề, **bold**, `code` hoặc link dạng Markdown.
                Return JSON object exactly:
                {
                  "content": "câu trả lời cuối cùng gửi cho người dùng, plain text ngắn gọn",
                  "confidence": 0-100,
                  "nextSteps": ["0-4 bước tiếp theo ngắn"],
                  "learningResources": [
                    {
                      "provider": "Coursera | W3Schools | freeCodeCamp | Google",
                      "title": "tên bài học/khóa học nên học",
                      "url": "link khóa học hoặc trang tìm kiếm chính thức",
                      "targetSkill": "kỹ năng cần bổ sung",
                      "reason": "vì sao phù hợp với người dùng"
                    }
                  ]
                }
                Nếu người dùng hỏi về nghề/kỹ năng/lộ trình, hãy gợi ý bài học thị trường phù hợp. Ưu tiên link chính thức Coursera hoặc W3Schools; không bịa link cụ thể nếu không chắc.
                Payload:
                %s
                """.formatted(toJson(payload));
    }

    private Map<String, Object> contextAnalysisPayload(ContextAnalysis contextAnalysis) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("intent", contextAnalysis.intent().name());
        payload.put("answerMode", contextAnalysis.answerMode());
        payload.put("contextSummary", contextAnalysis.contextSummary());
        payload.put("ragQuery", contextAnalysis.ragQuery());
        payload.put("responseGuidance", contextAnalysis.responseGuidance());
        payload.put("shouldUseCareerContext", contextAnalysis.shouldUseCareerContext());
        payload.put("confidence", contextAnalysis.confidence());
        payload.put("nextSteps", contextAnalysis.nextSteps());
        return payload;
    }

    private ContextAnalysis parseContextAnalysis(String content) {
        try {
            JsonNode node = objectMapper.readTree(content);
            return new ContextAnalysis(
                    parseIntent(requiredText(node, "intent", "LLM đọc context")),
                    requiredText(node, "answerMode", "LLM đọc context"),
                    requiredText(node, "contextSummary", "LLM đọc context"),
                    requiredText(node, "ragQuery", "LLM đọc context"),
                    requiredText(node, "responseGuidance", "LLM đọc context"),
                    requiredBoolean(node, "shouldUseCareerContext", "LLM đọc context"),
                    requiredConfidence(node, "LLM đọc context"),
                    requiredStringList(node, "nextSteps", "LLM đọc context")
            );
        } catch (IllegalArgumentException | JsonProcessingException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "LLM đọc context trả dữ liệu không hợp lệ");
        }
    }

    private ParsedAssistantResponse parseAssistantResponse(String content) {
        try {
            JsonNode node = objectMapper.readTree(content);
            return new ParsedAssistantResponse(
                    plainChatContent(requiredText(node, "content", "LLM trả lời")),
                    requiredConfidence(node, "LLM trả lời"),
                    requiredStringList(node, "nextSteps", "LLM trả lời"),
                    learningResourceService.parseFromLlm(objectMapper.convertValue(node.path("learningResources"), Object.class))
            );
        } catch (IllegalArgumentException | JsonProcessingException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "LLM trả lời trả dữ liệu không hợp lệ");
        }
    }

    private ChatIntent parseIntent(String value) {
        try {
            return ChatIntent.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Invalid intent " + value, exception);
        }
    }

    private String requireSuccessfulLlm(LlmResponse response, String stage) {
        if (response == null || !response.success() || !StringUtils.hasText(response.content())) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, stage + " thất bại: " + llmErrorMessage(response));
        }
        return response.content();
    }

    private String llmErrorMessage(LlmResponse response) {
        if (response == null || !StringUtils.hasText(response.errorMessage())) {
            return "không nhận được phản hồi từ provider. Kiểm tra LLM_PROVIDER và GEMINI_API_KEY/LLM_API_KEY.";
        }
        String message = response.errorMessage();
        if (message.length() > 500) {
            return message.substring(0, 500) + "...";
        }
        return message;
    }

    private String requiredText(JsonNode root, String field, String stage) {
        String value = root.path(field).asText();
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(stage + " missing " + field);
        }
        return value;
    }

    private boolean requiredBoolean(JsonNode root, String field, String stage) {
        JsonNode value = root.path(field);
        if (!value.isBoolean()) {
            throw new IllegalArgumentException(stage + " missing " + field);
        }
        return value.asBoolean();
    }

    private int requiredConfidence(JsonNode root, String stage) {
        JsonNode value = root.path("confidence");
        if (!value.canConvertToInt()) {
            throw new IllegalArgumentException(stage + " missing confidence");
        }
        return Math.max(0, Math.min(100, value.asInt()));
    }

    private List<String> requiredStringList(JsonNode root, String field, String stage) {
        JsonNode value = root.path(field);
        if (!value.isArray()) {
            throw new IllegalArgumentException(stage + " missing " + field);
        }
        return objectMapper.convertValue(value, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
    }

    private ConversationSession getOwnedSession(UUID userId, UUID sessionId) {
        return sessionRepository.findByIdAndUserId(sessionId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Chat session not found"));
    }

    private ChatSessionResponse toSessionResponse(ConversationSession session) {
        return new ChatSessionResponse(session.getId(), session.getTitle(), session.getCreatedAt(), session.getUpdatedAt());
    }

    private ConversationMessageResponse toMessageResponse(ConversationMessage message) {
        return new ConversationMessageResponse(
                message.getId(),
                message.getSender().name(),
                message.getContent(),
                message.getIntent() == null ? null : message.getIntent().name(),
                message.getConfidence() == null ? null : message.getConfidence().intValue(),
                fromJsonList(message.getSources()),
                message.getCreatedAt()
        );
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "[]";
        }
    }

    private List<String> fromJsonList(String value) {
        if (value == null) {
            return List.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            return List.of();
        }
    }

    private String plainChatContent(String value) {
        String plain = value
                .replace("```", "")
                .replaceAll("(?m)^\\s{0,3}#{1,6}\\s*", "")
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("__(.*?)__", "$1")
                .replaceAll("\\*(.*?)\\*", "$1")
                .replaceAll("_(.*?)_", "$1")
                .replaceAll("`([^`]*)`", "$1")
                .replaceAll("\\[([^\\]]+)]\\(([^)]+)\\)", "$1")
                .replaceAll("(?m)^\\s*[-*+]\\s+", "")
                .replaceAll("(?m)^\\s*\\d+[.)]\\s+", "")
                .replaceAll("[\\r\\n]+", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (plain.length() <= 700) {
            return plain;
        }
        int cut = Math.max(420, plain.lastIndexOf('.', 700));
        return plain.substring(0, Math.min(cut + 1, plain.length())).trim();
    }

    private record ContextAnalysis(
            ChatIntent intent,
            String answerMode,
            String contextSummary,
            String ragQuery,
            String responseGuidance,
            boolean shouldUseCareerContext,
            int confidence,
            List<String> nextSteps
    ) {
    }

    private record ParsedAssistantResponse(
            String content,
            int confidence,
            List<String> nextSteps,
            List<LearningResourceResponse> learningResources
    ) {
    }
}
