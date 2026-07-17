package com.careercompass.service;

import com.careercompass.dto.response.CareerDetailResponse;
import com.careercompass.dto.response.CareerElementResponse;
import com.careercompass.dto.response.CareerSummaryResponse;
import com.careercompass.dto.response.CareerTaskResponse;
import com.careercompass.dto.response.MarketSignalResponse;
import com.careercompass.dto.response.PageResponse;
import com.careercompass.dto.response.RelatedCareerResponse;
import com.careercompass.dto.response.TechnologySkillResponse;
import com.careercompass.entity.ElementCategory;
import com.careercompass.entity.Occupation;
import com.careercompass.entity.OccupationElementScore;
import com.careercompass.entity.OccupationTask;
import com.careercompass.entity.OccupationTechnologySkill;
import com.careercompass.entity.OnetElement;
import com.careercompass.entity.RelatedOccupation;
import com.careercompass.entity.TechnologySkill;
import com.careercompass.exception.ApiException;
import com.careercompass.mapper.OccupationMapper;
import com.careercompass.repository.OccupationElementScoreRepository;
import com.careercompass.repository.OccupationRepository;
import com.careercompass.repository.OccupationTaskRepository;
import com.careercompass.repository.OccupationTechnologySkillRepository;
import com.careercompass.repository.OnetElementRepository;
import com.careercompass.repository.RelatedOccupationRepository;
import com.careercompass.repository.TechnologySkillRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CareerService {

    private final OccupationRepository occupationRepository;
    private final OccupationElementScoreRepository elementScoreRepository;
    private final OccupationTaskRepository taskRepository;
    private final OccupationTechnologySkillRepository occupationTechnologySkillRepository;
    private final TechnologySkillRepository technologySkillRepository;
    private final RelatedOccupationRepository relatedOccupationRepository;
    private final OnetElementRepository onetElementRepository;
    private final OccupationMapper occupationMapper;

    @Transactional(readOnly = true)
    public PageResponse<CareerSummaryResponse> search(String query, String cluster, Pageable pageable) {
        String normalizedQuery = StringUtils.hasText(query) ? query.trim() : null;
        String normalizedCluster = StringUtils.hasText(cluster) ? cluster.trim() : null;
        return PageResponse.from(occupationRepository.search(normalizedQuery, normalizedCluster, pageable)
                .map(occupationMapper::toSummary));
    }

    @Transactional(readOnly = true)
    public CareerDetailResponse detail(String onetCode) {
        Occupation occupation = getOccupation(onetCode);
        return new CareerDetailResponse(
                occupation.getOnetCode(),
                occupation.getTitleVi(),
                occupation.getTitleEn(),
                occupation.getDescription(),
                occupation.getCareerCluster(),
                occupation.getJobZone(),
                elementsByCategory(onetCode, ElementCategory.SKILL),
                elementsByCategory(onetCode, ElementCategory.ABILITY),
                elementsByCategory(onetCode, ElementCategory.INTEREST),
                elementsByCategory(onetCode, ElementCategory.WORK_STYLE),
                technologySkills(onetCode),
                tasks(onetCode),
                related(onetCode),
                List.<MarketSignalResponse>of()
        );
    }

    @Transactional(readOnly = true)
    public List<CareerElementResponse> elements(String onetCode) {
        getOccupation(onetCode);
        List<CareerElementResponse> result = new ArrayList<>();
        result.addAll(elementsByCategory(onetCode, ElementCategory.INTEREST));
        result.addAll(elementsByCategory(onetCode, ElementCategory.ABILITY));
        result.addAll(elementsByCategory(onetCode, ElementCategory.SKILL));
        result.addAll(elementsByCategory(onetCode, ElementCategory.WORK_STYLE));
        return result;
    }

    @Transactional(readOnly = true)
    public List<TechnologySkillResponse> technologySkills(String onetCode) {
        getOccupation(onetCode);
        List<OccupationTechnologySkill> links = occupationTechnologySkillRepository.findByOnetCodeOrderByRequiredScoreDesc(onetCode);
        Map<Long, TechnologySkill> skillsById = technologySkillRepository.findAllById(
                        links.stream().map(OccupationTechnologySkill::getTechnologySkillId).toList())
                .stream()
                .collect(Collectors.toMap(TechnologySkill::getId, Function.identity()));
        return links.stream()
                .map(link -> {
                    TechnologySkill skill = skillsById.get(link.getTechnologySkillId());
                    return new TechnologySkillResponse(skill.getId(), skill.getSkillName(), skill.getCategory(), link.getRequiredScore());
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CareerTaskResponse> tasks(String onetCode) {
        getOccupation(onetCode);
        return taskRepository.findByOnetCodeOrderByImportanceScoreDesc(onetCode).stream()
                .map(task -> new CareerTaskResponse(task.getId(), task.getTaskText(), task.getImportanceScore()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RelatedCareerResponse> related(String onetCode) {
        getOccupation(onetCode);
        return relatedOccupationRepository.findByOnetCode(onetCode).stream()
                .map(this::toRelatedCareer)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> careerClusters() {
        return occupationRepository.findCareerClusters();
    }

    private List<CareerElementResponse> elementsByCategory(String onetCode, ElementCategory category) {
        List<OccupationElementScore> scores = elementScoreRepository.findTop5ByOnetCodeAndCategoryOrderByScoreDesc(onetCode, category);
        Map<String, OnetElement> elements = onetElementRepository.findAllById(
                        scores.stream().map(OccupationElementScore::getElementId).toList())
                .stream()
                .collect(Collectors.toMap(OnetElement::getElementId, Function.identity()));
        return scores.stream()
                .map(score -> {
                    OnetElement element = elements.get(score.getElementId());
                    return new CareerElementResponse(
                            score.getElementId(),
                            element == null ? score.getElementId() : element.getElementName(),
                            score.getCategory().name(),
                            score.getScore()
                    );
                })
                .toList();
    }

    private RelatedCareerResponse toRelatedCareer(RelatedOccupation relatedOccupation) {
        Occupation related = getOccupation(relatedOccupation.getRelatedOnetCode());
        return new RelatedCareerResponse(
                related.getOnetCode(),
                related.getTitleVi(),
                related.getTitleEn(),
                relatedOccupation.getRelationType()
        );
    }

    private Occupation getOccupation(String onetCode) {
        return occupationRepository.findById(onetCode)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Career not found"));
    }
}
