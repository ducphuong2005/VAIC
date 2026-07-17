package com.careercompass.service;

import com.careercompass.dto.request.CreateChatSessionRequest;
import com.careercompass.dto.request.SendChatMessageRequest;
import com.careercompass.dto.response.ChatMessageResponse;
import com.careercompass.dto.response.ChatSessionDetailResponse;
import com.careercompass.dto.response.ChatSessionResponse;
import com.careercompass.dto.response.ConversationMessageResponse;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationSessionRepository sessionRepository;
    private final ConversationMessageRepository messageRepository;
    private final RagRetrievalLogRepository retrievalLogRepository;
    private final RagRetriever ragRetriever;
    private final LlmService llmService;
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
        ChatIntent intent = detectIntent(request.content());
        messageRepository.save(ConversationMessage.builder()
                .sessionId(session.getId())
                .sender(MessageSender.USER)
                .content(request.content())
                .intent(intent)
                .build());

        List<RagDocumentResult> documents = ragRetriever.retrieve(new RagQuery(request.content(), null, "vi", null, null, null, 5));
        List<String> sources = documents.stream().map(RagDocumentResult::title).toList();
        retrievalLogRepository.save(RagRetrievalLog.builder()
                .userId(userId)
                .queryText(request.content())
                .filtersPayload("{}")
                .resultsPayload(toJson(sources))
                .build());

        String prompt = buildPrompt(request.content(), intent, documents);
        LlmResponse llmResponse = llmService.generate(LlmPurpose.CHAT_RESPONSE, prompt);
        ParsedAssistantResponse parsed = parseAssistantResponse(llmResponse.content());
        String guardedContent = guardrail(parsed.content());
        ConversationMessage assistant = messageRepository.save(ConversationMessage.builder()
                .sessionId(session.getId())
                .sender(MessageSender.ASSISTANT)
                .content(guardedContent)
                .intent(intent)
                .confidence(BigDecimal.valueOf(parsed.confidence()))
                .sources(toJson(sources))
                .build());
        session.setTitle(session.getTitle() == null ? "Career chat" : session.getTitle());
        sessionRepository.save(session);
        return new ChatMessageResponse(assistant.getId(), guardedContent, intent.name(), List.of(), List.of(), sources, parsed.confidence());
    }

    private ChatIntent detectIntent(String content) {
        String text = content.toLowerCase();
        if (text.contains("skill") || text.contains("thiếu")) {
            return ChatIntent.SKILL_GAP;
        }
        if (text.contains("so sánh")) {
            return ChatIntent.CAREER_COMPARISON;
        }
        if (text.contains("lộ trình") || text.contains("học")) {
            return ChatIntent.LEARNING_PATH;
        }
        if (text.contains("thị trường") || text.contains("lương")) {
            return ChatIntent.MARKET_QUESTION;
        }
        if (text.contains("nghề") || text.contains("phù hợp")) {
            return ChatIntent.CAREER_RECOMMENDATION;
        }
        return ChatIntent.GENERAL_CAREER_CHAT;
    }

    private String buildPrompt(String message, ChatIntent intent, List<RagDocumentResult> documents) {
        return "Intent: " + intent + "\nUser: " + message + "\nSources: " + documents.stream()
                .map(doc -> doc.title() + ": " + doc.content())
                .toList()
                + "\nReturn JSON with content and confidence. Avoid absolute claims and provide multiple options.";
    }

    private String guardrail(String content) {
        return content
                .replace("Bạn chắc chắn chỉ phù hợp với nghề này.", "Đây là một trong các lựa chọn bạn có thể khám phá.")
                .replace("Bạn không thể làm nghề", "Bạn có thể cần bổ sung kỹ năng trước khi theo nghề");
    }

    private ParsedAssistantResponse parseAssistantResponse(String content) {
        try {
            JsonNode node = objectMapper.readTree(content);
            return new ParsedAssistantResponse(
                    node.path("content").asText("Dựa trên dữ liệu hiện tại, bạn có thể khám phá thêm nhiều lựa chọn nghề nghiệp."),
                    node.path("confidence").asInt(60)
            );
        } catch (JsonProcessingException exception) {
            return new ParsedAssistantResponse("Dựa trên dữ liệu hiện tại, bạn có thể khám phá thêm nhiều lựa chọn nghề nghiệp.", 50);
        }
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

    private record ParsedAssistantResponse(String content, int confidence) {
    }
}
