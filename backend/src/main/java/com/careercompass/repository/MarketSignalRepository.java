package com.careercompass.repository;

import com.careercompass.entity.MarketSignal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketSignalRepository extends JpaRepository<MarketSignal, Long> {

    List<MarketSignal> findTop20ByOrderByDemandScoreDesc();

    List<MarketSignal> findByOnetCodeOrderByLastUpdatedAtDesc(String onetCode);

    List<MarketSignal> findByOnetCodeAndRegionOrderByLastUpdatedAtDesc(String onetCode, String region);

    Optional<MarketSignal> findByOnetCodeAndRegionAndSourceAndPeriodLabel(String onetCode, String region, String source, String periodLabel);
}
