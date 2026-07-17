package com.careercompass.repository;

import com.careercompass.entity.AssessmentQuestion;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentQuestionRepository extends JpaRepository<AssessmentQuestion, Long> {

    List<AssessmentQuestion> findByAssessmentIdOrderByDisplayOrderAsc(UUID assessmentId);
}
