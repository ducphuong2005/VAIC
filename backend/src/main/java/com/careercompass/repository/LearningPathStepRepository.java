package com.careercompass.repository;

import com.careercompass.entity.LearningPathStep;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningPathStepRepository extends JpaRepository<LearningPathStep, Long> {
    List<LearningPathStep> findByLearningPathIdOrderByStepOrderAsc(UUID learningPathId);
    Optional<LearningPathStep> findByIdAndLearningPathId(Long id, UUID learningPathId);
}
