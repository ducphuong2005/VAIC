package com.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "career_recommendations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "run_id", nullable = false, length = 36)
    private UUID runId;

    @Column(name = "onet_code", nullable = false, length = 20)
    private String onetCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "recommendation_group", nullable = false, length = 50)
    private RecommendationGroup recommendationGroup;

    @Column(name = "rank_number", nullable = false)
    private int rankNumber;

    @Column(name = "interest_score", nullable = false)
    private BigDecimal interestScore;

    @Column(name = "ability_score", nullable = false)
    private BigDecimal abilityScore;

    @Column(name = "skill_score", nullable = false)
    private BigDecimal skillScore;

    @Column(name = "work_style_score", nullable = false)
    private BigDecimal workStyleScore;

    @Column(name = "market_score", nullable = false)
    private BigDecimal marketScore;

    @Column(name = "feasibility_score", nullable = false)
    private BigDecimal feasibilityScore;

    @Column(name = "final_score", nullable = false)
    private BigDecimal finalScore;

    @Column(name = "confidence", nullable = false)
    private BigDecimal confidence;

    @Column(name = "reasons")
    private String reasons;

    @Column(name = "considerations")
    private String considerations;

    @Column(name = "sources")
    private String sources;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
