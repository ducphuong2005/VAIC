package com.careercompass.repository;

import com.careercompass.entity.LearningPath;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningPathRepository extends JpaRepository<LearningPath, UUID> {
    List<LearningPath> findByUserIdOrderByUpdatedAtDesc(UUID userId);
    Optional<LearningPath> findByIdAndUserId(UUID id, UUID userId);
    Optional<LearningPath> findTopByUserIdAndOnetCodeOrderByUpdatedAtDesc(UUID userId, String onetCode);
}
