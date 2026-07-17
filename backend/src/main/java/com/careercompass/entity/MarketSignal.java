package com.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "market_signals")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketSignal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "onet_code", nullable = false, length = 20)
    private String onetCode;

    @Column(name = "region", nullable = false, length = 120)
    private String region;

    @Column(name = "source", nullable = false, length = 80)
    private String source;

    @Column(name = "period_label", nullable = false, length = 50)
    private String periodLabel;

    @Column(name = "job_count", nullable = false)
    private int jobCount;

    @Column(name = "growth_rate")
    private BigDecimal growthRate;

    @Column(name = "median_salary")
    private BigDecimal medianSalary;

    @Column(name = "entry_level_ratio")
    private BigDecimal entryLevelRatio;

    @Column(name = "remote_ratio")
    private BigDecimal remoteRatio;

    @Column(name = "demand_score", nullable = false)
    private BigDecimal demandScore;

    @Column(name = "data_confidence", nullable = false)
    private BigDecimal dataConfidence;

    @Column(name = "sample_size", nullable = false)
    private int sampleSize;

    @Column(name = "last_updated_at", nullable = false)
    private Instant lastUpdatedAt;
}
