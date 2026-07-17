package com.careercompass.repository;

import com.careercompass.entity.CareerRecommendation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CareerRecommendationRepository extends JpaRepository<CareerRecommendation, Long> {

    List<CareerRecommendation> findByRunIdOrderByRankNumberAsc(UUID runId);

    Optional<CareerRecommendation> findByIdAndRunId(Long id, UUID runId);
}
