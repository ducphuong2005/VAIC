package com.careercompass.repository;

import com.careercompass.entity.AssessmentOption;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentOptionRepository extends JpaRepository<AssessmentOption, Long> {

    List<AssessmentOption> findByQuestionIdOrderByDisplayOrderAsc(Long questionId);

    List<AssessmentOption> findByIdIn(Collection<Long> ids);
}
