package com.careercompass.service;

import com.careercompass.dto.request.GenerateRecommendationRequest;
import com.careercompass.dto.request.RecommendationFeedbackRequest;
import com.careercompass.dto.response.RecommendationDetailResponse;
import com.careercompass.dto.response.RecommendationFeedbackResponse;
import com.careercompass.dto.response.RecommendationRunResponse;
import com.careercompass.dto.response.RecommendationScoreResponse;
import com.careercompass.dto.response.SkillGapResponse;
import com.careercompass.entity.CareerRecommendation;
import com.careercompass.entity.Occupation;
import com.careercompass.entity.OccupationTechnologySkill;
import com.careercompass.entity.RecommendationEvidence;
import com.careercompass.entity.RecommendationFeedback;
import com.careercompass.entity.RecommendationRun;
import com.careercompass.entity.RecommendationRunStatus;
import com.careercompass.entity.SkillGap;
import com.careercompass.entity.StudentProfile;
import com.careercompass.entity.TechnologySkill;
import com.careercompass.entity.UserExistingSkill;
import com.careercompass.entity.UserProfileDimension;
import com.careercompass.exception.ApiException;
import com.careercompass.recommendation.RecommendationCandidate;
import com.careercompass.recommendation.RecommendationScoringEngine;
import com.careercompass.recommendation.SkillGapCalculator;
import com.careercompass.repository.CareerRecommendationRepository;
import com.careercompass.repository.OccupationRepository;
import com.careercompass.repository.OccupationTechnologySkillRepository;
import com.careercompass.repository.RecommendationEvidenceRepository;
import com.careercompass.repository.RecommendationFeedbackRepository;
import com.careercompass.repository.RecommendationRunRepository;
import com.careercompass.repository.SkillGapRepository;
import com.careercompass.repository.StudentProfileRepository;
import com.careercompass.repository.TechnologySkillRepository;
import com.careercompass.repository.UserExistingSkillRepository;
import com.careercompass.repository.UserProfileDimensionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
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
public class RecommendationService {

    private static final int DEFAULT_LIMIT = 10;

    private final RecommendationRunRepository runRepository;
    private final CareerRecommendationRepository recommendationRepository;
    private final RecommendationEvidenceRepository evidenceRepository;
    private final RecommendationFeedbackRepository feedbackRepository;
    private final SkillGapRepository skillGapRepository;
    private final OccupationRepository occupationRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final UserProfileDimensionRepository dimensionRepository;
    private final UserExistingSkillRepository userExistingSkillRepository;
    private final OccupationTechnologySkillRepository occupationTechnologySkillRepository;
    private final TechnologySkillRepository technologySkillRepository;
    private final RecommendationScoringEngine scoringEngine;
    private final SkillGapCalculator skillGapCalculator;
    private final ObjectMapper objectMapper;

    @Transactional
    public RecommendationRunResponse generate(UUID userId, GenerateRecommendationRequest request) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
        List<UserProfileDimension> dimensions = dimensionRepository.findByUserIdOrderByElementIdAsc(userId);
        List<UserExistingSkill> userSkills = userExistingSkillRepository.findByUserIdOrderBySkillNameAsc(userId);
        Map<String, UserProfileDimension> dimensionsByElement = dimensions.stream()
                .collect(Collectors.toMap(UserProfileDimension::getElementId, Function.identity()));
        BigDecimal profileConfidence = averageConfidence(dimensions);
        RecommendationRun run = runRepository.save(RecommendationRun.builder()
                .userId(userId)
                .profileSnapshot(toJson(Map.of(
                        "dimensionCount", dimensions.size(),
                        "skillCount", userSkills.size(),
                        "region", request.region() == null ? "" : request.region()
                )))
                .profileConfidence(profileConfidence)
                .status(RecommendationRunStatus.COMPLETED)
                .build());

        int limit = request.limit() == null ? DEFAULT_LIMIT : request.limit();
        List<RecommendationCandidate> candidates = occupationRepository.findByActiveTrueOrderByTitleViAsc().stream()
                .map(occupation -> scoringEngine.score(occupation, dimensionsByElement, userSkills, profile, request.region()))
                .sorted((left, right) -> right.finalScore().compareTo(left.finalScore()))
                .limit(limit)
                .toList();

        int rank = 1;
        for (RecommendationCandidate candidate : candidates) {
            CareerRecommendation recommendation = recommendationRepository.save(toEntity(run.getId(), rank, candidate));
            evidenceRepository.saveAll(List.of(
                    RecommendationEvidence.builder()
                            .recommendationId(recommendation.getId())
                            .evidenceType("SCORE")
                            .evidenceText("Final score calculated by deterministic backend engine.")
                            .score(candidate.finalScore())
                            .build(),
                    RecommendationEvidence.builder()
                            .recommendationId(recommendation.getId())
                            .evidenceType("FAIRNESS")
                            .evidenceText("Gender, ethnicity and hometown were not used for ability, interest or skill fit.")
                            .build()
            ));
            createSkillGaps(recommendation.getId(), candidate.occupation().getOnetCode(), userSkills);
            rank++;
        }
        return run(userId, run.getId());
    }

    @Transactional(readOnly = true)
    public RecommendationRunResponse latest(UUID userId) {
        RecommendationRun run = runRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "No recommendation run found"));
        return toRunResponse(run);
    }

    @Transactional(readOnly = true)
    public RecommendationRunResponse run(UUID userId, UUID runId) {
        RecommendationRun run = getOwnedRun(userId, runId);
        return toRunResponse(run);
    }

    @Transactional(readOnly = true)
    public RecommendationDetailResponse detail(UUID userId, Long id) {
        CareerRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Recommendation not found"));
        getOwnedRun(userId, recommendation.getRunId());
        return toDetail(recommendation);
    }

    @Transactional
    public RecommendationFeedbackResponse feedback(UUID userId, Long id, RecommendationFeedbackRequest request) {
        CareerRecommendation recommendation = recommendationRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Recommendation not found"));
        getOwnedRun(userId, recommendation.getRunId());
        RecommendationFeedback feedback = feedbackRepository.save(RecommendationFeedback.builder()
                .recommendationId(id)
                .userId(userId)
                .rating(request.rating())
                .feedbackText(request.feedbackText())
                .build());
        return new RecommendationFeedbackResponse(
                feedback.getId(),
                feedback.getRecommendationId(),
                feedback.getRating(),
                feedback.getFeedbackText(),
                feedback.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<SkillGapResponse> skillGaps(UUID userId, Long recommendationId) {
        CareerRecommendation recommendation = recommendationRepository.findById(recommendationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Recommendation not found"));
        getOwnedRun(userId, recommendation.getRunId());
        return skillGapRepository.findByRecommendationIdOrderByGapScoreDesc(recommendationId).stream()
                .map(this::toSkillGapResponse)
                .toList();
    }

    private CareerRecommendation toEntity(UUID runId, int rank, RecommendationCandidate candidate) {
        return CareerRecommendation.builder()
                .runId(runId)
                .onetCode(candidate.occupation().getOnetCode())
                .recommendationGroup(candidate.group())
                .rankNumber(rank)
                .interestScore(candidate.interestScore())
                .abilityScore(candidate.abilityScore())
                .skillScore(candidate.skillScore())
                .workStyleScore(candidate.workStyleScore())
                .marketScore(candidate.marketScore())
                .feasibilityScore(candidate.feasibilityScore())
                .finalScore(candidate.finalScore())
                .confidence(candidate.confidence())
                .reasons(toJson(candidate.reasons()))
                .considerations(toJson(candidate.considerations()))
                .sources(toJson(candidate.sources()))
                .build();
    }

    private void createSkillGaps(Long recommendationId, String onetCode, List<UserExistingSkill> userSkills) {
        Map<String, BigDecimal> currentSkills = userSkills.stream()
                .collect(Collectors.toMap(
                        skill -> normalize(skill.getSkillName()),
                        UserExistingSkill::getScore,
                        BigDecimal::max
                ));
        Map<Long, TechnologySkill> techById = technologySkillRepository.findAllById(
                        occupationTechnologySkillRepository.findByOnetCodeOrderByRequiredScoreDesc(onetCode).stream()
                                .map(OccupationTechnologySkill::getTechnologySkillId)
                                .toList())
                .stream()
                .collect(Collectors.toMap(TechnologySkill::getId, Function.identity()));
        List<SkillGap> gaps = occupationTechnologySkillRepository.findByOnetCodeOrderByRequiredScoreDesc(onetCode).stream()
                .map(required -> {
                    TechnologySkill skill = techById.get(required.getTechnologySkillId());
                    if (skill == null) {
                        return null;
                    }
                    BigDecimal current = currentSkills.getOrDefault(normalize(skill.getSkillName()), BigDecimal.ZERO);
                    BigDecimal requiredScore = required.getRequiredScore() == null ? BigDecimal.valueOf(70) : required.getRequiredScore();
                    return skillGapCalculator.build(recommendationId, skill.getSkillName(), current, requiredScore);
                })
                .filter(gap -> gap != null && gap.getGapScore().compareTo(BigDecimal.ZERO) > 0)
                .toList();
        skillGapRepository.saveAll(gaps);
    }

    private RecommendationRunResponse toRunResponse(RecommendationRun run) {
        return new RecommendationRunResponse(
                run.getId(),
                run.getProfileConfidence().doubleValue(),
                recommendationRepository.findByRunIdOrderByRankNumberAsc(run.getId()).stream()
                        .map(this::toDetail)
                        .toList()
        );
    }

    private RecommendationDetailResponse toDetail(CareerRecommendation recommendation) {
        Occupation occupation = occupationRepository.findById(recommendation.getOnetCode())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Career not found"));
        return new RecommendationDetailResponse(
                recommendation.getId(),
                recommendation.getOnetCode(),
                occupation.getTitleVi(),
                recommendation.getRecommendationGroup().name(),
                recommendation.getRankNumber(),
                new RecommendationScoreResponse(
                        recommendation.getInterestScore().doubleValue(),
                        recommendation.getAbilityScore().doubleValue(),
                        recommendation.getSkillScore().doubleValue(),
                        recommendation.getWorkStyleScore().doubleValue(),
                        recommendation.getMarketScore().doubleValue(),
                        recommendation.getFeasibilityScore().doubleValue(),
                        recommendation.getFinalScore().doubleValue()
                ),
                recommendation.getConfidence().doubleValue(),
                fromJsonList(recommendation.getReasons()),
                fromJsonList(recommendation.getConsiderations()),
                skillGapRepository.findByRecommendationIdOrderByGapScoreDesc(recommendation.getId()).stream()
                        .map(this::toSkillGapResponse)
                        .toList(),
                List.of("Market score uses TopCV sample data when available."),
                fromJsonList(recommendation.getSources())
        );
    }

    private SkillGapResponse toSkillGapResponse(SkillGap gap) {
        return new SkillGapResponse(
                gap.getId(),
                gap.getSkillName(),
                gap.getCurrentScore().doubleValue(),
                gap.getRequiredScore().doubleValue(),
                gap.getGapScore().doubleValue(),
                gap.getPriority().name(),
                fromJsonList(gap.getSuggestedCourses())
        );
    }

    private RecommendationRun getOwnedRun(UUID userId, UUID runId) {
        RecommendationRun run = runRepository.findById(runId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Recommendation run not found"));
        if (!run.getUserId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Recommendation run belongs to another user");
        }
        return run;
    }

    private BigDecimal averageConfidence(List<UserProfileDimension> dimensions) {
        double confidence = dimensions.stream()
                .mapToDouble(dimension -> dimension.getConfidence().doubleValue())
                .average()
                .orElse(40);
        return BigDecimal.valueOf(confidence).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
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
}
