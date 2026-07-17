package com.careercompass.service;

import com.careercompass.dto.request.ExistingSkillRequest;
import com.careercompass.dto.request.ProfileUpdateRequest;
import com.careercompass.dto.response.ExistingSkillResponse;
import com.careercompass.dto.response.ProfileCompletionResponse;
import com.careercompass.dto.response.ProfileDimensionResponse;
import com.careercompass.dto.response.ProfileEvidenceResponse;
import com.careercompass.dto.response.ProfileResponse;
import com.careercompass.entity.OnetElement;
import com.careercompass.entity.ProfileEvidence;
import com.careercompass.entity.StudentProfile;
import com.careercompass.entity.UserExistingSkill;
import com.careercompass.entity.UserProfileDimension;
import com.careercompass.repository.OnetElementRepository;
import com.careercompass.repository.ProfileEvidenceRepository;
import com.careercompass.repository.StudentProfileRepository;
import com.careercompass.repository.UserExistingSkillRepository;
import com.careercompass.repository.UserProfileDimensionRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final StudentProfileRepository studentProfileRepository;
    private final UserExistingSkillRepository userExistingSkillRepository;
    private final UserProfileDimensionRepository userProfileDimensionRepository;
    private final ProfileEvidenceRepository profileEvidenceRepository;
    private final OnetElementRepository onetElementRepository;
    private final ProfileAggregationService profileAggregationService;

    @Transactional(readOnly = true)
    public ProfileResponse getProfile(UUID userId) {
        return toProfileResponse(studentProfileRepository.findByUserId(userId).orElse(null), userId);
    }

    @Transactional
    public ProfileResponse updateProfile(UUID userId, ProfileUpdateRequest request) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId)
                .orElseGet(() -> StudentProfile.builder().userId(userId).build());
        profile.setEducationLevel(trim(request.educationLevel()));
        profile.setSchool(trim(request.school()));
        profile.setCurrentMajor(trim(request.currentMajor()));
        profile.setPreferredRegions(join(request.preferredRegions()));
        profile.setLearningBudget(request.learningBudget());
        profile.setAvailableLearningHoursPerWeek(request.availableLearningHoursPerWeek());
        profile.setPreferredEducationRoutes(join(request.preferredEducationRoutes()));
        profile.setCareerGoals(trim(request.careerGoals()));
        StudentProfile savedProfile = studentProfileRepository.save(profile);

        userExistingSkillRepository.deleteByUserId(userId);
        if (request.existingSkills() != null) {
            List<UserExistingSkill> skills = request.existingSkills().stream()
                    .map(skill -> toSkill(userId, skill))
                    .toList();
            userExistingSkillRepository.saveAll(skills);
        }
        return toProfileResponse(savedProfile, userId);
    }

    @Transactional(readOnly = true)
    public List<ProfileDimensionResponse> dimensions(UUID userId) {
        Map<String, OnetElement> elements = onetElementRepository.findAllById(
                        userProfileDimensionRepository.findByUserIdOrderByElementIdAsc(userId)
                                .stream()
                                .map(UserProfileDimension::getElementId)
                                .toList())
                .stream()
                .collect(Collectors.toMap(OnetElement::getElementId, Function.identity()));

        return userProfileDimensionRepository.findByUserIdOrderByElementIdAsc(userId).stream()
                .map(dimension -> toDimensionResponse(dimension, elements.get(dimension.getElementId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProfileEvidenceResponse> evidence(UUID userId) {
        return profileEvidenceRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toEvidenceResponse)
                .toList();
    }

    @Transactional
    public List<ProfileDimensionResponse> recalculate(UUID userId) {
        List<ProfileEvidence> evidenceList = profileEvidenceRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Map<String, List<ProfileEvidence>> byElement = evidenceList.stream()
                .collect(Collectors.groupingBy(ProfileEvidence::getElementId));
        Instant now = Instant.now();
        for (Map.Entry<String, List<ProfileEvidence>> entry : byElement.entrySet()) {
            ProfileAggregationResult result = profileAggregationService.aggregate(entry.getValue());
            UserProfileDimension dimension = userProfileDimensionRepository.findByUserIdAndElementId(userId, entry.getKey())
                    .orElseGet(() -> UserProfileDimension.builder().userId(userId).elementId(entry.getKey()).build());
            dimension.setScore(result.score());
            dimension.setConfidence(result.confidence());
            dimension.setEvidenceCount(result.evidenceCount());
            dimension.setLastCalculatedAt(now);
            userProfileDimensionRepository.save(dimension);
        }
        return dimensions(userId);
    }

    @Transactional(readOnly = true)
    public ProfileCompletionResponse completion(UUID userId) {
        StudentProfile profile = studentProfileRepository.findByUserId(userId).orElse(null);
        List<String> missing = new ArrayList<>();
        if (profile == null || !StringUtils.hasText(profile.getEducationLevel())) {
            missing.add("educationLevel");
        }
        if (profile == null || !StringUtils.hasText(profile.getSchool())) {
            missing.add("school");
        }
        if (profile == null || !StringUtils.hasText(profile.getCurrentMajor())) {
            missing.add("currentMajor");
        }
        if (profile == null || !StringUtils.hasText(profile.getCareerGoals())) {
            missing.add("careerGoals");
        }
        if (userExistingSkillRepository.findByUserIdOrderBySkillNameAsc(userId).isEmpty()) {
            missing.add("existingSkills");
        }
        int total = 5;
        int completed = total - missing.size();
        int percent = Math.round((completed * 100f) / total);
        String status = percent >= 80 ? "GOOD" : percent >= 50 ? "PARTIAL" : "STARTED";
        return new ProfileCompletionResponse(percent, missing, status);
    }

    private ProfileResponse toProfileResponse(StudentProfile profile, UUID userId) {
        return new ProfileResponse(
                userId,
                profile == null ? null : profile.getEducationLevel(),
                profile == null ? null : profile.getSchool(),
                profile == null ? null : profile.getCurrentMajor(),
                split(profile == null ? null : profile.getPreferredRegions()),
                profile == null ? null : profile.getLearningBudget(),
                profile == null ? null : profile.getAvailableLearningHoursPerWeek(),
                split(profile == null ? null : profile.getPreferredEducationRoutes()),
                profile == null ? null : profile.getCareerGoals(),
                userExistingSkillRepository.findByUserIdOrderBySkillNameAsc(userId).stream()
                        .map(skill -> new ExistingSkillResponse(skill.getId(), skill.getSkillName(), skill.getScore()))
                        .toList(),
                profile == null ? null : profile.getUpdatedAt()
        );
    }

    private ProfileDimensionResponse toDimensionResponse(UserProfileDimension dimension, OnetElement element) {
        return new ProfileDimensionResponse(
                dimension.getElementId(),
                element == null ? dimension.getElementId() : element.getElementName(),
                element == null ? null : element.getCategory().name(),
                dimension.getScore(),
                dimension.getConfidence(),
                dimension.getEvidenceCount(),
                dimension.getLastCalculatedAt()
        );
    }

    private ProfileEvidenceResponse toEvidenceResponse(ProfileEvidence evidence) {
        return new ProfileEvidenceResponse(
                evidence.getId(),
                evidence.getSourceType().name(),
                evidence.getSourceId(),
                evidence.getElementId(),
                evidence.getEvidenceScore(),
                evidence.getEvidenceConfidence(),
                evidence.getEvidencePayload(),
                evidence.getCreatedAt()
        );
    }

    private UserExistingSkill toSkill(UUID userId, ExistingSkillRequest request) {
        return UserExistingSkill.builder()
                .userId(userId)
                .skillName(request.skillName().trim())
                .score(request.score())
                .build();
    }

    private String trim(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String join(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.stream()
                .filter(StringUtils::hasText)
                .map(String::trim)
                .collect(Collectors.joining(","));
    }

    private List<String> split(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}
