package com.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "skill_gaps")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkillGap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recommendation_id", nullable = false)
    private Long recommendationId;

    @Column(name = "skill_name", nullable = false, length = 190)
    private String skillName;

    @Column(name = "current_score", nullable = false)
    private BigDecimal currentScore;

    @Column(name = "required_score", nullable = false)
    private BigDecimal requiredScore;

    @Column(name = "gap_score", nullable = false)
    private BigDecimal gapScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    private SkillGapPriority priority;

    @Column(name = "suggested_courses")
    private String suggestedCourses;
}
