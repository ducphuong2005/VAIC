package com.careercompass.repository;

import com.careercompass.entity.AssessmentAnswer;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentAnswerRepository extends JpaRepository<AssessmentAnswer, Long> {

    List<AssessmentAnswer> findBySessionId(UUID sessionId);
}
