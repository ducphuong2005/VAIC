package com.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "mini_game_metrics")
@Getter
@Setter
public class MiniGameMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "mini_game_id", nullable = false, length = 36)
    private UUID miniGameId;

    @Column(name = "metric_code", nullable = false, length = 80)
    private String metricCode;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @Column(name = "min_value", nullable = false)
    private BigDecimal minValue;

    @Column(name = "max_value", nullable = false)
    private BigDecimal maxValue;

    @Column(name = "higher_is_better", nullable = false)
    private boolean higherIsBetter;
}
