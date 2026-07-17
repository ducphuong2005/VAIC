package com.careercompass.repository;

import com.careercompass.entity.Assessment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    List<Assessment> findByActiveTrueOrderByTitleAsc();
}
