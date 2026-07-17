package com.careercompass.recommendation;

import com.careercompass.config.RecommendationProperties;
import com.careercompass.entity.ElementCategory;
import com.careercompass.entity.MarketSignal;
import com.careercompass.entity.Occupation;
import com.careercompass.entity.OccupationElementScore;
import com.careercompass.entity.OccupationTechnologySkill;
import com.careercompass.entity.RecommendationGroup;
import com.careercompass.entity.StudentProfile;
import com.careercompass.entity.TechnologySkill;
import com.careercompass.entity.UserExistingSkill;
import com.careercompass.entity.UserProfileDimension;
import com.careercompass.repository.MarketSignalRepository;
import com.careercompass.repository.OccupationElementScoreRepository;
import com.careercompass.repository.OccupationTechnologySkillRepository;
import com.careercompass.repository.TechnologySkillRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class RecommendationScoringEngine {

    private static final BigDecimal DEFAULT_SCORE = BigDecimal.valueOf(50);

    private final RecommendationProperties properties;
    private final OccupationElementScoreRepository elementScoreRepository;
    private final OccupationTechnologySkillRepository occupationTechnologySkillRepository;
    private final TechnologySkillRepository technologySkillRepository;
    private final MarketSignalRepository marketSignalRepository;

    public RecommendationCandidate score(
            Occupation occupation,
            Map<String, UserProfileDimension> profileDimensions,
            List<UserExistingSkill> userSkills,
            StudentProfile profile,
            String region
    ) {
        BigDecimal interest = elementMatch(occupation.getOnetCode(), ElementCategory.INTEREST, profileDimensions);
        BigDecimal ability = elementMatch(occupation.getOnetCode(), ElementCategory.ABILITY, profileDimensions);
        BigDecimal skill = skillMatch(occupation.getOnetCode(), userSkills);
        BigDecimal workStyle = elementMatch(occupation.getOnetCode(), ElementCategory.WORK_STYLE, profileDimensions);
        BigDecimal market = marketScore(occupation.getOnetCode(), region);
        BigDecimal feasibility = feasibilityScore(occupation, profile);
        BigDecimal finalScore = finalScore(interest, ability, skill, workStyle, market, feasibility);
        RecommendationGroup group = group(finalScore);
        BigDecimal confidence = confidence(profileDimensions, market);
        List<String> reasons = List.of(
                "Interest match: " + interest + "/100",
                "Ability match: " + ability + "/100",
                "Market demand sample score: " + market + "/100"
        );
        List<String> considerations = List.of(
                "Skill score: " + skill + "/100, review skill gaps before committing.",
                "Region only affects market availability, not ability or interest fit."
        );
        List<String> sources = List.of("O*NET occupation elements", "User profile evidence", "TopCV market sample");
        return new RecommendationCandidate(
                occupation,
                group,
                interest,
                ability,
                skill,
                workStyle,
                market,
                feasibility,
                finalScore,
                confidence,
                reasons,
                considerations,
                sources
        );
    }

    /**
     * Computes the required recommendation formula:
     * finalScore = interest * 0.25 + ability * 0.25 + skill * 0.15
     * + workStyle * 0.10 + market * 0.15 + feasibility * 0.10.
     * Weights are configurable from application properties.
     */
    public BigDecimal finalScore(
            BigDecimal interest,
            BigDecimal ability,
            BigDecimal skill,
            BigDecimal workStyle,
            BigDecimal market,
            BigDecimal feasibility
    ) {
        RecommendationProperties.Weights weights = properties.weights();
        double score = interest.doubleValue() * weights.interest()
                + ability.doubleValue() * weights.ability()
                + skill.doubleValue() * weights.skill()
                + workStyle.doubleValue() * weights.workStyle()
                + market.doubleValue() * weights.market()
                + feasibility.doubleValue() * weights.feasibility();
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal elementMatch(String onetCode, ElementCategory category, Map<String, UserProfileDimension> profileDimensions) {
        List<OccupationElementScore> required = elementScoreRepository.findByOnetCodeAndCategory(onetCode, category);
        if (required.isEmpty()) {
            return DEFAULT_SCORE;
        }
        double averageSimilarity = required.stream()
                .mapToDouble(score -> {
                    UserProfileDimension dimension = profileDimensions.get(score.getElementId());
                    if (dimension == null) {
                        return DEFAULT_SCORE.doubleValue();
                    }
                    double diff = Math.abs(dimension.getScore().doubleValue() - score.getScore().doubleValue());
                    return Math.max(0, 100 - diff);
                })
                .average()
                .orElse(DEFAULT_SCORE.doubleValue());
        return BigDecimal.valueOf(averageSimilarity).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal skillMatch(String onetCode, List<UserExistingSkill> userSkills) {
        List<OccupationTechnologySkill> requiredSkills = occupationTechnologySkillRepository.findByOnetCodeOrderByRequiredScoreDesc(onetCode);
        if (requiredSkills.isEmpty()) {
            return DEFAULT_SCORE;
        }
        Map<Long, TechnologySkill> techById = technologySkillRepository.findAllById(
                        requiredSkills.stream().map(OccupationTechnologySkill::getTechnologySkillId).toList())
                .stream()
                .collect(Collectors.toMap(TechnologySkill::getId, Function.identity()));
        Map<String, BigDecimal> userSkillScores = userSkills.stream()
                .collect(Collectors.toMap(
                        skill -> normalize(skill.getSkillName()),
                        UserExistingSkill::getScore,
                        BigDecimal::max
                ));
        double average = requiredSkills.stream()
                .mapToDouble(required -> {
                    TechnologySkill skill = techById.get(required.getTechnologySkillId());
                    if (skill == null) {
                        return DEFAULT_SCORE.doubleValue();
                    }
                    BigDecimal current = userSkillScores.getOrDefault(normalize(skill.getSkillName()), BigDecimal.ZERO);
                    BigDecimal requiredScore = Optional.ofNullable(required.getRequiredScore()).orElse(BigDecimal.valueOf(70));
                    if (requiredScore.compareTo(BigDecimal.ZERO) <= 0) {
                        return DEFAULT_SCORE.doubleValue();
                    }
                    return current.multiply(BigDecimal.valueOf(100))
                            .divide(requiredScore, 2, RoundingMode.HALF_UP)
                            .min(BigDecimal.valueOf(100))
                            .doubleValue();
                })
                .average()
                .orElse(DEFAULT_SCORE.doubleValue());
        return BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal marketScore(String onetCode, String region) {
        List<MarketSignal> signals = StringUtils.hasText(region)
                ? marketSignalRepository.findByOnetCodeAndRegionOrderByLastUpdatedAtDesc(onetCode, region.trim())
                : marketSignalRepository.findByOnetCodeOrderByLastUpdatedAtDesc(onetCode);
        return signals.stream()
                .findFirst()
                .map(MarketSignal::getDemandScore)
                .orElse(DEFAULT_SCORE);
    }

    private BigDecimal feasibilityScore(Occupation occupation, StudentProfile profile) {
        int jobZone = occupation.getJobZone() == null ? 3 : occupation.getJobZone();
        double base = Math.max(40, 100 - Math.max(0, jobZone - 2) * 12);
        if (profile != null && profile.getAvailableLearningHoursPerWeek() != null && profile.getAvailableLearningHoursPerWeek() >= 10) {
            base += 8;
        }
        if (profile != null && profile.getLearningBudget() != null && profile.getLearningBudget().compareTo(BigDecimal.ZERO) > 0) {
            base += 5;
        }
        return BigDecimal.valueOf(Math.min(100, base)).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal confidence(Map<String, UserProfileDimension> profileDimensions, BigDecimal market) {
        double profileConfidence = profileDimensions.values().stream()
                .mapToDouble(dimension -> dimension.getConfidence().doubleValue())
                .average()
                .orElse(40);
        double blended = profileConfidence * 0.8 + market.doubleValue() * 0.2;
        return BigDecimal.valueOf(Math.min(95, blended)).setScale(2, RoundingMode.HALF_UP);
    }

    private RecommendationGroup group(BigDecimal finalScore) {
        if (finalScore.compareTo(BigDecimal.valueOf(75)) >= 0) {
            return RecommendationGroup.DIRECT_MATCH;
        }
        if (finalScore.compareTo(BigDecimal.valueOf(60)) >= 0) {
            return RecommendationGroup.GROWTH_OPTION;
        }
        return RecommendationGroup.EXPLORATION_OPTION;
    }

    private String normalize(String value) {
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
