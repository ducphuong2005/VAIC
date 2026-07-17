package com.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "learning_path_steps")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LearningPathStep {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "learning_path_id", nullable = false, length = 36)
    private UUID learningPathId;
    @Column(name = "step_order", nullable = false)
    private int stepOrder;
    @Column(name = "title", nullable = false)
    private String title;
    @Column(name = "description")
    private String description;
    @Column(name = "target_skill", length = 190)
    private String targetSkill;
    @Column(name = "course_id")
    private Long courseId;
    @Column(name = "duration_hours")
    private Integer durationHours;
    @Column(name = "progress_percent", nullable = false)
    private int progressPercent;
    @Column(name = "completed", nullable = false)
    private boolean completed;
}
