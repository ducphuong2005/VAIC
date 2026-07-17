package com.careercompass.repository;

import com.careercompass.entity.MiniGameMetricResult;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiniGameMetricResultRepository extends JpaRepository<MiniGameMetricResult, Long> {

    List<MiniGameMetricResult> findBySessionId(UUID sessionId);
}
