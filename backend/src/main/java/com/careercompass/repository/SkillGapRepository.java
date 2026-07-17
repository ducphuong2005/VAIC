package com.careercompass.repository;

import com.careercompass.entity.SkillGap;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkillGapRepository extends JpaRepository<SkillGap, Long> {

    List<SkillGap> findByRecommendationIdOrderByGapScoreDesc(Long recommendationId);
}
