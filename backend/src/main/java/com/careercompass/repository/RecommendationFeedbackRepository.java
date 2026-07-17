package com.careercompass.repository;

import com.careercompass.entity.RecommendationFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, Long> {
}
