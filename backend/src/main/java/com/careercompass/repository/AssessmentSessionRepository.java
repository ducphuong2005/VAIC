package com.careercompass.repository;

import com.careercompass.entity.AssessmentSession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AssessmentSessionRepository extends JpaRepository<AssessmentSession, UUID> {
}
