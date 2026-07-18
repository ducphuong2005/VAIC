package com.careercompass.service;

import com.careercompass.dto.request.CompleteMiniGameRequest;
import com.careercompass.dto.request.RecordMiniGameActionsRequest;
import com.careercompass.dto.response.MiniGameActionResponse;
import com.careercompass.dto.response.MiniGameDetailResponse;
import com.careercompass.dto.response.MiniGameMetricResponse;
import com.careercompass.dto.response.MiniGameResultMetricResponse;
import com.careercompass.dto.response.MiniGameResultResponse;
import com.careercompass.dto.response.MiniGameSessionResponse;
import com.careercompass.dto.response.MiniGameSummaryResponse;
import com.careercompass.dto.response.AiCareerAdviceResponse;
import com.careercompass.entity.EvidenceSourceType;
import com.careercompass.entity.MiniGame;
import com.careercompass.entity.MiniGameAction;
import com.careercompass.entity.MiniGameMetric;
import com.careercompass.entity.MiniGameMetricResult;
import com.careercompass.entity.MiniGameOnetMapping;
import com.careercompass.entity.MiniGameSession;
import com.careercompass.entity.ProfileEvidence;
import com.careercompass.entity.SessionStatus;
import com.careercompass.exception.ApiException;
import com.careercompass.repository.MiniGameActionRepository;
import com.careercompass.repository.MiniGameMetricRepository;
import com.careercompass.repository.MiniGameMetricResultRepository;
import com.careercompass.repository.MiniGameOnetMappingRepository;
import com.careercompass.repository.MiniGameRepository;
import com.careercompass.repository.MiniGameSessionRepository;
import com.careercompass.repository.ProfileEvidenceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MiniGameService {

    private static final BigDecimal GAME_EVIDENCE_CONFIDENCE = BigDecimal.valueOf(75);

    private final MiniGameRepository miniGameRepository;
    private final MiniGameSessionRepository sessionRepository;
    private final MiniGameActionRepository actionRepository;
    private final MiniGameMetricRepository metricRepository;
    private final MiniGameMetricResultRepository resultRepository;
    private final MiniGameOnetMappingRepository mappingRepository;
    private final ProfileEvidenceRepository evidenceRepository;
    private final MiniGameNormalizationService normalizationService;
    private final ProfileService profileService;
    private final AiCareerAdviceService aiCareerAdviceService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<MiniGameSummaryResponse> list() {
        return miniGameRepository.findByActiveTrueOrderByTitleAsc().stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public MiniGameDetailResponse detail(UUID id) {
        MiniGame game = getGame(id);
        return new MiniGameDetailResponse(
                game.getId(),
                game.getCode(),
                game.getTitle(),
                game.getDescription(),
                game.getGameType().name(),
                game.getEstimatedMinutes(),
                metricRepository.findByMiniGameId(game.getId()).stream().map(this::toMetricResponse).toList()
        );
    }

    @Transactional
    public MiniGameSessionResponse start(UUID userId, UUID miniGameId) {
        getGame(miniGameId);
        MiniGameSession session = MiniGameSession.builder()
                .userId(userId)
                .miniGameId(miniGameId)
                .status(SessionStatus.IN_PROGRESS)
                .startedAt(Instant.now())
                .build();
        return toSessionResponse(sessionRepository.save(session));
    }

    @Transactional
    public List<MiniGameActionResponse> recordActions(UUID userId, UUID sessionId, RecordMiniGameActionsRequest request) {
        MiniGameSession session = getOwnedSession(userId, sessionId);
        ensureInProgress(session);
        List<MiniGameAction> actions = request.actions().stream()
                .map(action -> MiniGameAction.builder()
                        .sessionId(sessionId)
                        .actionType(action.actionType())
                        .actionPayload(toJson(action.actionPayload()))
                        .occurredAt(Instant.now())
                        .build())
                .toList();
        return actionRepository.saveAll(actions).stream()
                .map(this::toActionResponse)
                .toList();
    }

    @Transactional
    public MiniGameResultResponse complete(UUID userId, UUID sessionId, CompleteMiniGameRequest request) {
        MiniGameSession session = getOwnedSession(userId, sessionId);
        ensureInProgress(session);
        Map<String, MiniGameMetric> metricsByCode = metricRepository.findByMiniGameId(session.getMiniGameId()).stream()
                .collect(Collectors.toMap(MiniGameMetric::getMetricCode, Function.identity()));

        List<MiniGameMetricResult> results = request.rawMetrics().entrySet().stream()
                .filter(entry -> metricsByCode.containsKey(entry.getKey()))
                .map(entry -> {
                    MiniGameMetric metric = metricsByCode.get(entry.getKey());
                    return MiniGameMetricResult.builder()
                            .sessionId(sessionId)
                            .metricCode(entry.getKey())
                            .rawValue(entry.getValue())
                            .normalizedScore(normalizationService.normalize(entry.getValue(), metric))
                            .build();
                })
                .toList();
        if (results.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "No valid game metrics");
        }

        resultRepository.saveAll(results);
        session.setStatus(SessionStatus.COMPLETED);
        session.setCompletedAt(Instant.now());
        session.setRawMetricsPayload(toJson(request.rawMetrics()));
        session.setNormalizedMetricsPayload(toJson(results.stream().collect(Collectors.toMap(
                MiniGameMetricResult::getMetricCode,
                MiniGameMetricResult::getNormalizedScore
        ))));
        session.setResultSummary(request.resultSummary());
        sessionRepository.save(session);

        int evidenceCreated = createEvidence(userId, sessionId, results, metricsByCode);
        profileService.recalculate(userId);
        AiCareerAdviceResponse advice = aiCareerAdviceService.buildAdviceAndPublish(
                userId,
                "MINIGAME_COMPLETED",
                session.getNormalizedMetricsPayload()
        );
        return result(userId, sessionId, evidenceCreated, advice);
    }

    @Transactional(readOnly = true)
    public MiniGameResultResponse result(UUID userId, UUID sessionId) {
        return result(userId, sessionId, 0);
    }

    private MiniGameResultResponse result(UUID userId, UUID sessionId, int evidenceCreated) {
        return result(userId, sessionId, evidenceCreated, null);
    }

    private MiniGameResultResponse result(UUID userId, UUID sessionId, int evidenceCreated, AiCareerAdviceResponse generatedAdvice) {
        MiniGameSession session = getOwnedSession(userId, sessionId);
        List<MiniGameResultMetricResponse> metrics = resultRepository.findBySessionId(sessionId).stream()
                .map(result -> new MiniGameResultMetricResponse(result.getMetricCode(), result.getRawValue(), result.getNormalizedScore()))
                .toList();
        AiCareerAdviceResponse advice = generatedAdvice != null
                ? generatedAdvice
                : session.getStatus() == SessionStatus.COMPLETED
                        ? aiCareerAdviceService.buildAdvice(userId, "MINIGAME_RESULT_VIEWED", session.getNormalizedMetricsPayload())
                        : null;
        return new MiniGameResultResponse(
                sessionId,
                session.getStatus().name(),
                session.getResultSummary(),
                metrics,
                evidenceCreated,
                advice
        );
    }

    private int createEvidence(UUID userId, UUID sessionId, List<MiniGameMetricResult> results, Map<String, MiniGameMetric> metricsByCode) {
        Map<Long, MiniGameMetricResult> resultByMetricId = results.stream()
                .collect(Collectors.toMap(result -> metricsByCode.get(result.getMetricCode()).getId(), Function.identity()));
        List<MiniGameOnetMapping> mappings = mappingRepository.findByMiniGameMetricIdIn(resultByMetricId.keySet());
        List<ProfileEvidence> evidence = mappings.stream()
                .map(mapping -> {
                    MiniGameMetricResult result = resultByMetricId.get(mapping.getMiniGameMetricId());
                    BigDecimal weightedScore = result.getNormalizedScore().multiply(mapping.getWeight()).min(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP);
                    return ProfileEvidence.builder()
                            .userId(userId)
                            .sourceType(EvidenceSourceType.MINI_GAME)
                            .sourceId(sessionId.toString())
                            .elementId(mapping.getElementId())
                            .evidenceScore(weightedScore)
                            .evidenceConfidence(GAME_EVIDENCE_CONFIDENCE)
                            .evidencePayload(result.getMetricCode())
                            .build();
                })
                .toList();
        evidenceRepository.saveAll(evidence);
        return evidence.size();
    }

    private MiniGameSummaryResponse toSummary(MiniGame game) {
        return new MiniGameSummaryResponse(
                game.getId(),
                game.getCode(),
                game.getTitle(),
                game.getDescription(),
                game.getGameType().name(),
                game.getEstimatedMinutes()
        );
    }

    private MiniGameMetricResponse toMetricResponse(MiniGameMetric metric) {
        return new MiniGameMetricResponse(
                metric.getMetricCode(),
                metric.getMetricName(),
                metric.getMinValue(),
                metric.getMaxValue(),
                metric.isHigherIsBetter()
        );
    }

    private MiniGameSessionResponse toSessionResponse(MiniGameSession session) {
        return new MiniGameSessionResponse(
                session.getId(),
                session.getMiniGameId(),
                session.getStatus().name(),
                session.getStartedAt(),
                session.getCompletedAt()
        );
    }

    private MiniGameActionResponse toActionResponse(MiniGameAction action) {
        return new MiniGameActionResponse(action.getId(), action.getActionType(), action.getActionPayload(), action.getOccurredAt());
    }

    private MiniGame getGame(UUID id) {
        return miniGameRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Mini-game not found"));
    }

    private MiniGameSession getOwnedSession(UUID userId, UUID sessionId) {
        MiniGameSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Mini-game session not found"));
        if (!session.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Session belongs to another user");
        }
        return session;
    }

    private void ensureInProgress(MiniGameSession session) {
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Session already completed");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot serialize mini-game payload");
        }
    }
}
