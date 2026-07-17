package com.careercompass.repository;

import com.careercompass.entity.MiniGameMetric;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiniGameMetricRepository extends JpaRepository<MiniGameMetric, Long> {

    List<MiniGameMetric> findByMiniGameId(UUID miniGameId);

    Optional<MiniGameMetric> findByMiniGameIdAndMetricCode(UUID miniGameId, String metricCode);
}
