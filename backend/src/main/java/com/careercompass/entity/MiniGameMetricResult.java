package com.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "mini_game_metric_results")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MiniGameMetricResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "session_id", nullable = false, length = 36)
    private UUID sessionId;

    @Column(name = "metric_code", nullable = false, length = 80)
    private String metricCode;

    @Column(name = "raw_value", nullable = false)
    private BigDecimal rawValue;

    @Column(name = "normalized_score", nullable = false)
    private BigDecimal normalizedScore;
}
