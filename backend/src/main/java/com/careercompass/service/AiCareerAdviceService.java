package com.careercompass.service;

import com.careercompass.dto.response.AiCareerAdviceResponse;
import com.careercompass.dto.response.AiCareerOptionResponse;
import com.careercompass.dto.response.JobPostingResponse;
import com.careercompass.dto.response.LearningResourceResponse;
import com.careercompass.entity.CareerRecommendation;
import com.careercompass.entity.JobPosting;
import com.careercompass.entity.JobPostingSkill;
import com.careercompass.entity.LearningPath;
import com.careercompass.entity.LearningPathStep;
import com.careercompass.entity.LlmPurpose;
import com.careercompass.entity.Occupation;
import com.careercompass.entity.OccupationTechnologySkill;
import com.careercompass.entity.RecommendationEvidence;
import com.careercompass.entity.RecommendationGroup;
import com.careercompass.entity.RecommendationRun;
import com.careercompass.entity.RecommendationRunStatus;
import com.careercompass.entity.SkillGap;
import com.careercompass.entity.TechnologySkill;
import com.careercompass.entity.StudentProfile;
import com.careercompass.entity.UserExistingSkill;
import com.careercompass.entity.UserProfileDimension;
import com.careercompass.exception.ApiException;
import com.careercompass.llm.LlmResponse;
import com.careercompass.rag.RagDocumentResult;
import com.careercompass.rag.RagQuery;
import com.careercompass.rag.RagRetriever;
import com.careercompass.recommendation.RecommendationCandidate;
import com.careercompass.recommendation.RecommendationScoringEngine;
import com.careercompass.repository.JobPostingRepository;
import com.careercompass.repository.JobPostingSkillRepository;
import com.careercompass.repository.CareerRecommendationRepository;
import com.careercompass.repository.LearningPathRepository;
import com.careercompass.repository.LearningPathStepRepository;
import com.careercompass.repository.OccupationTechnologySkillRepository;
import com.careercompass.repository.OccupationRepository;
import com.careercompass.repository.RecommendationEvidenceRepository;
import com.careercompass.repository.RecommendationRunRepository;
import com.careercompass.repository.SkillGapRepository;
import com.careercompass.repository.StudentProfileRepository;
import com.careercompass.repository.TechnologySkillRepository;
import com.careercompass.repository.UserExistingSkillRepository;
import com.careercompass.repository.UserProfileDimensionRepository;
import com.careercompass.recommendation.SkillGapCalculator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiCareerAdviceService {

    private static final Map<String, String> RIASEC_TYPES = Map.of(
            "RIASEC-R", "Realistic (Doers): thích hoạt động thực hành, công cụ, máy móc hoặc thao tác cụ thể.",
            "RIASEC-I", "Investigative (Thinkers): thích phân tích, quan sát, nghiên cứu và giải quyết vấn đề.",
            "RIASEC-A", "Artistic (Creators): thích sáng tạo, biểu đạt và môi trường ít khuôn mẫu.",
            "RIASEC-S", "Social (Helpers): thích hỗ trợ, giảng dạy, chữa lành hoặc tư vấn cho người khác.",
            "RIASEC-E", "Enterprising (Persuaders): thích dẫn dắt, thuyết phục và quản lý để đạt mục tiêu.",
            "RIASEC-C", "Conventional (Organizers): thích cấu trúc, dữ liệu, hồ sơ, chi tiết và quy trình rõ ràng."
    );

    private final StudentProfileRepository studentProfileRepository;
    private final UserProfileDimensionRepository dimensionRepository;
    private final UserExistingSkillRepository userExistingSkillRepository;
    private final OccupationRepository occupationRepository;
    private final JobPostingRepository jobPostingRepository;
    private final JobPostingSkillRepository jobPostingSkillRepository;
    private final RecommendationRunRepository recommendationRunRepository;
    private final CareerRecommendationRepository careerRecommendationRepository;
    private final RecommendationEvidenceRepository recommendationEvidenceRepository;
    private final SkillGapRepository skillGapRepository;
    private final OccupationTechnologySkillRepository occupationTechnologySkillRepository;
    private final TechnologySkillRepository technologySkillRepository;
    private final LearningPathRepository learningPathRepository;
    private final LearningPathStepRepository learningPathStepRepository;
    private final RecommendationScoringEngine scoringEngine;
    private final SkillGapCalculator skillGapCalculator;
    private final RagRetriever ragRetriever;
    private final JobMarketCsvService jobMarketCsvService;
    private final LearningResourceService learningResourceService;
    private final ActivityLogService activityLogService;
    private final LlmService llmService;
    private final ObjectMapper objectMapper;

    public AiCareerAdviceResponse buildAdvice(UUID userId, String trigger, String eventSummary) {
        CareerContext context = buildCareerContext(userId, trigger, eventSummary);
        List<RagDocumentResult> ragDocs = retrieveAdviceDocuments(context);

        String prompt = buildPrompt(context, ragDocs);
        LlmResponse response = llmService.generate(LlmPurpose.RECOMMENDATION_EXPLANATION, prompt);
        ParsedAdvice parsed = parseAdvice(requireSuccessfulLlm(response, "LLM tư vấn hướng nghiệp"));
        return new AiCareerAdviceResponse(
                parsed.content(),
                parsed.confidence(),
                context.topRiasecTypes(),
                context.careerOptions(),
                context.marketJobs(),
                parsed.nextSteps(),
                sources(ragDocs, context.marketJobs())
        );
    }

    public AiCareerAdviceResponse buildAdviceAndPublish(UUID userId, String trigger, String eventSummary) {
        CareerContext context = buildCareerContext(userId, trigger, eventSummary);
        List<RagDocumentResult> ragDocs = retrieveAdviceDocuments(context);

        String prompt = buildPrompt(context, ragDocs);
        LlmResponse response = llmService.generate(LlmPurpose.RECOMMENDATION_EXPLANATION, prompt);
        ParsedAdvice parsed = parseAdvice(requireSuccessfulLlm(response, "LLM tư vấn hướng nghiệp"));
        AiCareerAdviceResponse advice = new AiCareerAdviceResponse(
                parsed.content(),
                parsed.confidence(),
                context.topRiasecTypes(),
                context.careerOptions(),
                context.marketJobs(),
                parsed.nextSteps(),
                sources(ragDocs, context.marketJobs())
        );
        publishPersonalizedOutputs(userId, context, parsed);
        return advice;
    }

    public void publishCareerGuidance(
            UUID userId,
            CareerContext context,
            String activityType,
            String activityMessage,
            String adviceContent,
            int confidence,
            List<String> nextSteps,
            List<LearningResourceResponse> learningResources
    ) {
        publishPersonalizedOutputs(
                userId,
                context,
                new ParsedAdvice(
                        adviceContent,
                        Math.max(0, Math.min(100, confidence)),
                        nextSteps == null ? List.of() : nextSteps,
                        learningResources == null ? List.of() : learningResources
                ),
                activityType,
                activityMessage
        );
    }

    public CareerContext buildCareerContext(UUID userId, String trigger, String eventSummary) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
        List<UserProfileDimension> dimensions = dimensionRepository.findByUserIdOrderByElementIdAsc(userId);
        List<UserExistingSkill> skills = userExistingSkillRepository.findByUserIdOrderBySkillNameAsc(userId);
        Map<String, UserProfileDimension> dimensionsByElement = dimensions.stream()
                .collect(Collectors.toMap(UserProfileDimension::getElementId, Function.identity()));

        List<RecommendationCandidate> candidates = occupationRepository.findByActiveTrueOrderByTitleViAsc().stream()
                .map(occupation -> scoringEngine.score(occupation, dimensionsByElement, skills, profile, firstPreferredRegion(profile)))
                .sorted((left, right) -> right.finalScore().compareTo(left.finalScore()))
                .limit(5)
                .toList();
        List<AiCareerOptionResponse> careerOptions = candidates.stream()
                .map(this::toCareerOption)
                .toList();
        List<String> topRiasecTypes = topRiasecTypes(dimensions);
        List<String> onetCodes = careerOptions.stream().map(AiCareerOptionResponse::onetCode).toList();
        List<JobPostingResponse> marketJobs = marketJobs(careerOptions, topRiasecTypes, firstPreferredRegion(profile), onetCodes);
        return new CareerContext(
                trigger,
                eventSummary,
                profilePayload(profile),
                topRiasecTypes,
                careerOptions,
                marketJobs,
                firstPreferredRegion(profile)
        );
    }

    private List<JobPostingResponse> marketJobs(
            List<AiCareerOptionResponse> careerOptions,
            List<String> topRiasecTypes,
            String preferredRegion,
            List<String> onetCodes
    ) {
        String fallbackOnetCode = careerOptions.isEmpty() ? null : careerOptions.get(0).onetCode();
        List<JobPostingResponse> csvJobs = jobMarketCsvService.findRelevantSamples(careerOptions, topRiasecTypes, preferredRegion, 8).stream()
                .map(sample -> toCsvJobPostingResponse(sample, fallbackOnetCode))
                .toList();
        if (!csvJobs.isEmpty()) {
            return csvJobs;
        }
        if (onetCodes.isEmpty()) {
            return List.of();
        }
        return jobPostingRepository.findByActiveTrueAndOnetCodeInOrderByCrawledAtDesc(onetCodes, PageRequest.of(0, 6)).stream()
                .map(this::toJobPostingResponse)
                .toList();
    }

    private AiCareerOptionResponse toCareerOption(RecommendationCandidate candidate) {
        Occupation occupation = candidate.occupation();
        String reason = "Phù hợp với điểm mạnh hiện tại: interest " + candidate.interestScore()
                + "/100, ability " + candidate.abilityScore()
                + "/100, skill " + candidate.skillScore() + "/100.";
        return new AiCareerOptionResponse(
                occupation.getOnetCode(),
                occupation.getTitleVi(),
                occupation.getTitleEn(),
                candidate.finalScore(),
                candidate.group().name(),
                reason
        );
    }

    private List<String> topRiasecTypes(List<UserProfileDimension> dimensions) {
        List<String> knownScores = dimensions.stream()
                .filter(dimension -> RIASEC_TYPES.containsKey(dimension.getElementId()))
                .sorted(Comparator.comparing(UserProfileDimension::getScore).reversed())
                .limit(3)
                .map(dimension -> RIASEC_TYPES.get(dimension.getElementId()) + " Điểm hiện tại: " + dimension.getScore() + "/100.")
                .toList();
        if (!knownScores.isEmpty()) {
            return knownScores;
        }
        return RIASEC_TYPES.entrySet().stream()
                .filter(entry -> entry.getKey().equals("RIASEC-I") || entry.getKey().equals("RIASEC-C"))
                .map(Map.Entry::getValue)
                .toList();
    }

    private String buildPrompt(CareerContext context, List<RagDocumentResult> ragDocs) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("trigger", context.trigger());
        payload.put("eventSummary", context.eventSummary());
        payload.put("studentProfile", context.studentProfile());
        payload.put("riasecDefinitionsAndStrengths", context.topRiasecTypes());
        payload.put("rankedCareerOptions", context.careerOptions());
        payload.put("topcvMarketJobs", context.marketJobs());
        payload.put("ragDocuments", ragDocs.stream()
                .map(doc -> Map.of("title", doc.title(), "content", doc.content(), "onetCode", doc.onetCode() == null ? "" : doc.onetCode()))
                .toList());
        return """
                Bạn là cố vấn hướng nghiệp AI của Career Compass.
                Chỉ dùng dữ liệu trong payload để tư vấn; không bịa job, công ty, lương hoặc nguồn tuyển dụng.
                Dữ liệu việc làm là mẫu crawl từ TopCV, không đại diện toàn bộ thị trường.
                Luôn dựa trên 6 nhóm RIASEC: Realistic, Investigative, Artistic, Social, Enterprising, Conventional.
                Trả lời tiếng Việt, giọng hỗ trợ, tránh kết luận tuyệt đối, đưa nhiều hướng đi.
                Return JSON object exactly with fields:
                {
                  "content": "3-5 câu tư vấn tổng hợp",
                  "confidence": 0-100,
                  "nextSteps": ["3-5 bước hành động ngắn"],
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
                Ưu tiên link chính thức từ Coursera hoặc W3Schools. Không bịa link khóa học cụ thể nếu không chắc; dùng link search Coursera theo kỹ năng/nghề hoặc tutorial W3Schools phù hợp.
                Payload:
                %s
                """.formatted(toJson(payload));
    }

    private ParsedAdvice parseAdvice(String content) {
        try {
            JsonNode root = objectMapper.readTree(content);
            String advice = requiredText(root, "content", "LLM tư vấn");
            int confidence = requiredConfidence(root, "LLM tư vấn");
            List<String> nextSteps = requiredStringList(root, "nextSteps", "LLM tư vấn");
            List<LearningResourceResponse> learningResources = learningResourceService.parseFromLlm(
                    objectMapper.convertValue(root.path("learningResources"), Object.class)
            );
            return new ParsedAdvice(advice, confidence, nextSteps, learningResources);
        } catch (IllegalArgumentException | JsonProcessingException exception) {
            throw new ApiException(HttpStatus.BAD_GATEWAY, "LLM tư vấn trả dữ liệu không hợp lệ");
        }
    }

    private JobPostingResponse toJobPostingResponse(JobPosting posting) {
        List<String> skills = jobPostingSkillRepository.findByJobPostingId(posting.getId()).stream()
                .map(JobPostingSkill::getSkillName)
                .toList();
        return new JobPostingResponse(
                posting.getId(),
                posting.getSourceName(),
                posting.getExternalId(),
                posting.getTitle(),
                posting.getCompanyName(),
                posting.getLocation(),
                posting.getRegion(),
                posting.getSalaryMin(),
                posting.getSalaryMax(),
                posting.isRemote(),
                posting.isEntryLevel(),
                posting.getOnetCode(),
                posting.getPostedAt(),
                posting.getCrawledAt(),
                posting.getRawUrl(),
                skills
        );
    }

    private JobPostingResponse toCsvJobPostingResponse(JobMarketCsvService.JobMarketSample sample, String onetCode) {
        return new JobPostingResponse(
                null,
                "TopCV CSV",
                "jobs.csv:" + sample.rowNumber(),
                sample.jobTitle(),
                "",
                sample.city(),
                sample.city(),
                sample.salaryMin(),
                sample.salaryMax(),
                false,
                isEntryLevel(sample),
                onetCode,
                null,
                null,
                null,
                sample.skills()
        );
    }

    private boolean isEntryLevel(JobMarketCsvService.JobMarketSample sample) {
        String text = (sample.positionLevel() + " " + sample.experience()).toLowerCase(Locale.ROOT);
        return text.contains("không yêu cầu")
                || text.contains("chưa có kinh nghiệm")
                || text.contains("thực tập")
                || text.contains("sinh viên")
                || text.contains("lên đến 1 năm");
    }

    private void publishPersonalizedOutputs(UUID userId, CareerContext context, ParsedAdvice parsed) {
        publishPersonalizedOutputs(
                userId,
                context,
                parsed,
                "AI_MINIGAME_GUIDANCE_GENERATED",
                "AI guidance generated from mini-game and data/jobs.csv"
        );
    }

    private void publishPersonalizedOutputs(
            UUID userId,
            CareerContext context,
            ParsedAdvice parsed,
            String activityType,
            String activityMessage
    ) {
        if (context.careerOptions().isEmpty()) {
            return;
        }
        RecommendationRun run = recommendationRunRepository.save(RecommendationRun.builder()
                .userId(userId)
                .profileSnapshot(toJson(Map.of(
                        "trigger", context.trigger(),
                        "eventSummary", context.eventSummary() == null ? "" : context.eventSummary(),
                    "llmAdvice", parsed.content(),
                    "riasec", context.topRiasecTypes(),
                    "learningResources", parsed.learningResources(),
                    "jobMarketSampleSource", "data/jobs.csv",
                    "jobMarketSampleCount", jobMarketCsvService.sampleCount()
                )))
                .profileConfidence(BigDecimal.valueOf(parsed.confidence()).setScale(2, RoundingMode.HALF_UP))
                .status(RecommendationRunStatus.COMPLETED)
                .build());

        List<UserExistingSkill> userSkills = userExistingSkillRepository.findByUserIdOrderBySkillNameAsc(userId);
        int rank = 1;
        for (AiCareerOptionResponse option : context.careerOptions().stream().limit(5).toList()) {
            CareerRecommendation recommendation = careerRecommendationRepository.save(toRecommendationEntity(run.getId(), rank, option, parsed));
            recommendationEvidenceRepository.saveAll(List.of(
                    RecommendationEvidence.builder()
                            .recommendationId(recommendation.getId())
                            .evidenceType("LLM")
                            .evidenceText(parsed.content())
                            .score(BigDecimal.valueOf(parsed.confidence()))
                            .build(),
                    RecommendationEvidence.builder()
                            .recommendationId(recommendation.getId())
                            .evidenceType("MARKET")
                            .evidenceText(marketEvidenceText(context.marketJobs()))
                            .build()
            ));
            createSkillGaps(recommendation.getId(), option.onetCode(), userSkills);
            if (rank == 1) {
                createLearningPath(userId, option, parsed, context.marketJobs());
            }
            rank++;
        }
        activityLogService.log(userId, activityType, activityMessage, run.getId().toString());
    }

    private CareerRecommendation toRecommendationEntity(UUID runId, int rank, AiCareerOptionResponse option, ParsedAdvice parsed) {
        BigDecimal finalScore = normalizeScore(option.score());
        BigDecimal interest = finalScore;
        BigDecimal ability = scaled(finalScore, 0.96);
        BigDecimal skill = scaled(finalScore, 0.88);
        BigDecimal workStyle = scaled(finalScore, 0.92);
        BigDecimal market = scaled(finalScore, 0.94);
        BigDecimal feasibility = scaled(finalScore, 0.90);
        return CareerRecommendation.builder()
                .runId(runId)
                .onetCode(option.onetCode())
                .recommendationGroup(parseGroup(option.recommendationGroup()))
                .rankNumber(rank)
                .interestScore(interest)
                .abilityScore(ability)
                .skillScore(skill)
                .workStyleScore(workStyle)
                .marketScore(market)
                .feasibilityScore(feasibility)
                .finalScore(finalScore)
                .confidence(BigDecimal.valueOf(parsed.confidence()).setScale(2, RoundingMode.HALF_UP))
                .reasons(toJson(recommendationReasons(option, parsed)))
                .considerations(toJson(List.of(
                        "Dữ liệu việc làm dùng mẫu TopCV trong data/jobs.csv, không đại diện toàn bộ thị trường.",
                        "Nên kiểm chứng lại bằng thêm khảo sát hoặc mini-game để tăng độ tin cậy.",
                        "Lộ trình học tập được sinh từ nextSteps của LLM và có thể điều chỉnh theo thời gian của bạn."
                )))
                .sources(toJson(List.of("Career Compass LLM", "RIASEC profile evidence", "data/jobs.csv")))
                .build();
    }

    private List<String> recommendationReasons(AiCareerOptionResponse option, ParsedAdvice parsed) {
        List<String> reasons = new ArrayList<>();
        reasons.add(option.reason());
        reasons.add(parsed.content());
        parsed.nextSteps().stream().limit(2).forEach(reasons::add);
        return reasons.stream().filter(StringUtils::hasText).toList();
    }

    private RecommendationGroup parseGroup(String value) {
        try {
            return RecommendationGroup.valueOf(value);
        } catch (RuntimeException exception) {
            return RecommendationGroup.EXPLORATION_OPTION;
        }
    }

    private BigDecimal normalizeScore(BigDecimal value) {
        BigDecimal score = value == null ? BigDecimal.valueOf(60) : value;
        if (score.compareTo(BigDecimal.ONE) <= 0) {
            score = score.multiply(BigDecimal.valueOf(100));
        }
        return score.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaled(BigDecimal value, double factor) {
        return value.multiply(BigDecimal.valueOf(factor)).max(BigDecimal.ZERO).min(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
    }

    private String marketEvidenceText(List<JobPostingResponse> jobs) {
        if (jobs.isEmpty()) {
            return "Chưa có job sample phù hợp trong data/jobs.csv cho hướng này.";
        }
        return jobs.stream()
                .limit(3)
                .map(job -> job.title() + " tại " + (StringUtils.hasText(job.location()) ? job.location() : "không rõ khu vực"))
                .collect(Collectors.joining("; "));
    }

    private void createSkillGaps(Long recommendationId, String onetCode, List<UserExistingSkill> userSkills) {
        Map<String, BigDecimal> currentSkills = userSkills.stream()
                .collect(Collectors.toMap(
                        skill -> normalize(skill.getSkillName()),
                        UserExistingSkill::getScore,
                        BigDecimal::max
                ));
        List<OccupationTechnologySkill> requiredSkills = occupationTechnologySkillRepository.findByOnetCodeOrderByRequiredScoreDesc(onetCode);
        Map<Long, TechnologySkill> techById = technologySkillRepository.findAllById(
                        requiredSkills.stream().map(OccupationTechnologySkill::getTechnologySkillId).toList())
                .stream()
                .collect(Collectors.toMap(TechnologySkill::getId, Function.identity()));
        List<SkillGap> gaps = requiredSkills.stream()
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

    private void createLearningPath(UUID userId, AiCareerOptionResponse option, ParsedAdvice parsed, List<JobPostingResponse> jobs) {
        if (learningPathRepository.findTopByUserIdAndOnetCodeOrderByUpdatedAtDesc(userId, option.onetCode()).isPresent()) {
            return;
        }
        LearningPath path = learningPathRepository.save(LearningPath.builder()
                .userId(userId)
                .onetCode(option.onetCode())
                .title(limit("Lộ trình AI tới " + option.titleVi(), 255))
                .route("AI_MINIGAME")
                .status("ACTIVE")
                .build());
        List<String> llmSteps = new ArrayList<>(parsed.nextSteps().stream().filter(StringUtils::hasText).limit(4).toList());
        while (llmSteps.size() < 3) {
            llmSteps.add(supplementalStep(llmSteps.size(), option, jobs));
        }
        int order = 1;
        List<LearningPathStep> steps = new ArrayList<>();
        for (String stepText : llmSteps) {
            steps.add(LearningPathStep.builder()
                    .learningPathId(path.getId())
                    .stepOrder(order)
                    .title(limit(stepTitle(order, stepText), 255))
                    .description(stepText)
                    .targetSkill(limit(targetSkill(stepText, jobs), 190))
                    .courseId(null)
                    .resourceLinks(learningResourceService.resourceLinksPayload(
                            targetSkill(stepText, jobs),
                            option.titleVi(),
                            stepText,
                            parsed.learningResources()
                    ))
                    .durationHours(estimatedHours(order))
                    .progressPercent(0)
                    .completed(false)
                    .build());
            order++;
        }
        learningPathStepRepository.saveAll(steps);
    }

    private String supplementalStep(int index, AiCareerOptionResponse option, List<JobPostingResponse> jobs) {
        if (index == 0) {
            return "Đọc lại mô tả nghề " + option.titleVi() + " và ghi ra những nhiệm vụ bạn muốn thử trong 2 tuần tới.";
        }
        if (index == 1) {
            return "Chọn một kỹ năng xuất hiện nhiều trong job sample TopCV như " + targetSkill("", jobs) + " để luyện tập.";
        }
        return "Làm một mini project nhỏ và dùng kết quả đó để hỏi chatbot cách cải thiện portfolio.";
    }

    private String stepTitle(int order, String stepText) {
        String normalized = stepText.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= 70) {
            return normalized;
        }
        return switch (order) {
            case 1 -> "Xác nhận hướng nghề phù hợp";
            case 2 -> "Bổ sung kỹ năng thị trường";
            case 3 -> "Thực hành qua dự án nhỏ";
            default -> "Chuẩn bị portfolio và ứng tuyển";
        };
    }

    private String targetSkill(String stepText, List<JobPostingResponse> jobs) {
        String normalized = normalize(stepText);
        for (String skill : jobs.stream().flatMap(job -> job.skills().stream()).toList()) {
            if (StringUtils.hasText(skill) && (normalized.contains(normalize(skill)) || normalize(skill).length() >= 3)) {
                return skill;
            }
        }
        return "Phân tích dữ liệu";
    }

    private int estimatedHours(int order) {
        return switch (order) {
            case 1 -> 12;
            case 2 -> 24;
            case 3 -> 36;
            default -> 18;
        };
    }

    private String limit(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, Math.max(0, max - 1)).trim();
    }

    private List<String> sources(List<RagDocumentResult> ragDocs, List<JobPostingResponse> marketJobs) {
        List<String> ragSources = ragDocs.stream().map(RagDocumentResult::title).toList();
        if (marketJobs.isEmpty()) {
            return ragSources;
        }
        return java.util.stream.Stream.concat(ragSources.stream(), java.util.stream.Stream.of("data/jobs.csv"))
                .distinct()
                .toList();
    }

    private String firstPreferredRegion(StudentProfile profile) {
        if (profile == null || profile.getPreferredRegions() == null || profile.getPreferredRegions().isBlank()) {
            return null;
        }
        return profile.getPreferredRegions().split(",")[0].trim();
    }

    private Map<String, Object> profilePayload(StudentProfile profile) {
        return Map.of(
                "educationLevel", profile == null || profile.getEducationLevel() == null ? "" : profile.getEducationLevel(),
                "currentMajor", profile == null || profile.getCurrentMajor() == null ? "" : profile.getCurrentMajor(),
                "preferredRegions", profile == null || profile.getPreferredRegions() == null ? "" : profile.getPreferredRegions(),
                "careerGoals", profile == null || profile.getCareerGoals() == null ? "" : profile.getCareerGoals()
        );
    }

    private List<RagDocumentResult> retrieveAdviceDocuments(CareerContext context) {
        return ragRetriever.retrieve(new RagQuery(
                promptQuery(context.trigger(), context.topRiasecTypes(), context.careerOptions()),
                null,
                "vi",
                context.preferredRegion(),
                null,
                null,
                6
        ));
    }

    private String promptQuery(String trigger, List<String> topRiasecTypes, List<AiCareerOptionResponse> careerOptions) {
        return (trigger + " " + String.join(" ", topRiasecTypes) + " " + careerOptions.stream()
                .map(AiCareerOptionResponse::titleEn)
                .collect(Collectors.joining(" "))).toLowerCase(Locale.ROOT);
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
        List<String> result = objectMapper.convertValue(value, objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
        if (result.isEmpty()) {
            throw new IllegalArgumentException(stage + " empty " + field);
        }
        return result;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private record ParsedAdvice(
            String content,
            int confidence,
            List<String> nextSteps,
            List<LearningResourceResponse> learningResources
    ) {
    }

    public record CareerContext(
            String trigger,
            String eventSummary,
            Map<String, Object> studentProfile,
            List<String> topRiasecTypes,
            List<AiCareerOptionResponse> careerOptions,
            List<JobPostingResponse> marketJobs,
            String preferredRegion
    ) {
    }
}
