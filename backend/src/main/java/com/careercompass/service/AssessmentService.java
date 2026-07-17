package com.careercompass.service;

import com.careercompass.dto.request.SubmitAssessmentAnswersRequest;
import com.careercompass.dto.response.AssessmentDetailResponse;
import com.careercompass.dto.response.AssessmentOptionResponse;
import com.careercompass.dto.response.AssessmentQuestionResponse;
import com.careercompass.dto.response.AssessmentResultResponse;
import com.careercompass.dto.response.AssessmentSessionResponse;
import com.careercompass.dto.response.AssessmentSummaryResponse;
import com.careercompass.entity.Assessment;
import com.careercompass.entity.AssessmentAnswer;
import com.careercompass.entity.AssessmentOption;
import com.careercompass.entity.AssessmentQuestion;
import com.careercompass.entity.AssessmentSession;
import com.careercompass.entity.EvidenceSourceType;
import com.careercompass.entity.ProfileEvidence;
import com.careercompass.entity.SessionStatus;
import com.careercompass.exception.ApiException;
import com.careercompass.repository.AssessmentAnswerRepository;
import com.careercompass.repository.AssessmentOptionRepository;
import com.careercompass.repository.AssessmentQuestionRepository;
import com.careercompass.repository.AssessmentRepository;
import com.careercompass.repository.AssessmentSessionRepository;
import com.careercompass.repository.ProfileEvidenceRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
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
public class AssessmentService {

    private static final BigDecimal ASSESSMENT_EVIDENCE_CONFIDENCE = BigDecimal.valueOf(80);

    private final AssessmentRepository assessmentRepository;
    private final AssessmentQuestionRepository questionRepository;
    private final AssessmentOptionRepository optionRepository;
    private final AssessmentSessionRepository sessionRepository;
    private final AssessmentAnswerRepository answerRepository;
    private final ProfileEvidenceRepository evidenceRepository;
    private final AssessmentScoringService scoringService;
    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<AssessmentSummaryResponse> list() {
        return assessmentRepository.findByActiveTrueOrderByTitleAsc().stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public AssessmentDetailResponse detail(UUID id) {
        Assessment assessment = getAssessment(id);
        return new AssessmentDetailResponse(
                assessment.getId(),
                assessment.getCode(),
                assessment.getTitle(),
                assessment.getDescription(),
                assessment.getAssessmentType().name(),
                assessment.getEstimatedMinutes(),
                questions(assessment.getId())
        );
    }

    @Transactional
    public AssessmentSessionResponse start(UUID userId, UUID assessmentId) {
        getAssessment(assessmentId);
        AssessmentSession session = AssessmentSession.builder()
                .userId(userId)
                .assessmentId(assessmentId)
                .status(SessionStatus.IN_PROGRESS)
                .startedAt(Instant.now())
                .build();
        return toSessionResponse(sessionRepository.save(session));
    }

    @Transactional
    public AssessmentSessionResponse submitAnswers(UUID userId, UUID sessionId, SubmitAssessmentAnswersRequest request) {
        AssessmentSession session = getOwnedSession(userId, sessionId);
        ensureInProgress(session);
        List<AssessmentAnswer> answers = request.answers().stream()
                .map(answer -> AssessmentAnswer.builder()
                        .sessionId(sessionId)
                        .questionId(answer.questionId())
                        .optionId(answer.optionId())
                        .answeredAt(Instant.now())
                        .build())
                .toList();
        answerRepository.saveAll(answers);
        return toSessionResponse(session);
    }

    @Transactional
    public AssessmentResultResponse complete(UUID userId, UUID sessionId) {
        AssessmentSession session = getOwnedSession(userId, sessionId);
        ensureInProgress(session);
        List<AssessmentAnswer> answers = answerRepository.findBySessionId(sessionId);
        if (answers.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Assessment has no answers");
        }

        Map<Long, AssessmentOption> optionsById = optionRepository.findByIdIn(
                        answers.stream().map(AssessmentAnswer::getOptionId).toList())
                .stream()
                .collect(Collectors.toMap(AssessmentOption::getId, Function.identity()));
        List<AssessmentOption> selectedOptions = answers.stream()
                .map(answer -> optionsById.get(answer.getOptionId()))
                .toList();
        Map<String, Double> rawScores = scoringService.score(selectedOptions);
        Map<String, Double> normalizedScores = scoringService.normalize(rawScores);

        session.setStatus(SessionStatus.COMPLETED);
        session.setCompletedAt(Instant.now());
        session.setRawScorePayload(toJson(rawScores));
        session.setNormalizedScorePayload(toJson(normalizedScores));
        sessionRepository.save(session);

        List<ProfileEvidence> evidence = normalizedScores.entrySet().stream()
                .map(entry -> ProfileEvidence.builder()
                        .userId(userId)
                        .sourceType(EvidenceSourceType.ASSESSMENT)
                        .sourceId(sessionId.toString())
                        .elementId(entry.getKey())
                        .evidenceScore(BigDecimal.valueOf(entry.getValue()))
                        .evidenceConfidence(ASSESSMENT_EVIDENCE_CONFIDENCE)
                        .evidencePayload(session.getNormalizedScorePayload())
                        .build())
                .toList();
        evidenceRepository.saveAll(evidence);
        profileService.recalculate(userId);
        return new AssessmentResultResponse(sessionId, session.getStatus().name(), rawScores, normalizedScores, evidence.size());
    }

    @Transactional(readOnly = true)
    public AssessmentResultResponse result(UUID userId, UUID sessionId) {
        AssessmentSession session = getOwnedSession(userId, sessionId);
        return new AssessmentResultResponse(
                sessionId,
                session.getStatus().name(),
                fromJson(session.getRawScorePayload()),
                fromJson(session.getNormalizedScorePayload()),
                0
        );
    }

    private List<AssessmentQuestionResponse> questions(UUID assessmentId) {
        return questionRepository.findByAssessmentIdOrderByDisplayOrderAsc(assessmentId).stream()
                .map(question -> new AssessmentQuestionResponse(
                        question.getId(),
                        question.getQuestionText(),
                        question.getDisplayOrder(),
                        question.getQuestionType().name(),
                        optionRepository.findByQuestionIdOrderByDisplayOrderAsc(question.getId()).stream()
                                .map(option -> new AssessmentOptionResponse(option.getId(), option.getOptionText(), option.getDisplayOrder()))
                                .toList()
                ))
                .toList();
    }

    private AssessmentSummaryResponse toSummary(Assessment assessment) {
        return new AssessmentSummaryResponse(
                assessment.getId(),
                assessment.getCode(),
                assessment.getTitle(),
                assessment.getDescription(),
                assessment.getAssessmentType().name(),
                assessment.getEstimatedMinutes()
        );
    }

    private AssessmentSessionResponse toSessionResponse(AssessmentSession session) {
        return new AssessmentSessionResponse(
                session.getId(),
                session.getAssessmentId(),
                session.getStatus().name(),
                session.getStartedAt(),
                session.getCompletedAt()
        );
    }

    private Assessment getAssessment(UUID id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assessment not found"));
    }

    private AssessmentSession getOwnedSession(UUID userId, UUID sessionId) {
        AssessmentSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Assessment session not found"));
        if (!session.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Session belongs to another user");
        }
        return session;
    }

    private void ensureInProgress(AssessmentSession session) {
        if (session.getStatus() != SessionStatus.IN_PROGRESS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Session already completed");
        }
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot serialize assessment result");
        }
    }

    private Map<String, Double> fromJson(String value) {
        if (value == null) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(value, new TypeReference<>() {
            });
        } catch (JsonProcessingException exception) {
            return Map.of();
        }
    }
}
