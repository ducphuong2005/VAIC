package com.careercompass.repository;

import com.careercompass.entity.RecommendationEvidence;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationEvidenceRepository extends JpaRepository<RecommendationEvidence, Long> {

    List<RecommendationEvidence> findByRecommendationId(Long recommendationId);
}
