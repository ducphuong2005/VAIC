package com.careercompass.repository;

import com.careercompass.entity.RecommendationRun;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRunRepository extends JpaRepository<RecommendationRun, UUID> {

    Optional<RecommendationRun> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
