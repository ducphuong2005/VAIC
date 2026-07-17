package com.careercompass.repository;

import com.careercompass.entity.MiniGameOnetMapping;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiniGameOnetMappingRepository extends JpaRepository<MiniGameOnetMapping, Long> {

    List<MiniGameOnetMapping> findByMiniGameMetricIdIn(Collection<Long> metricIds);
}
