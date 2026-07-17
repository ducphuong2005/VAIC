package com.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fairness_test_runs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FairnessTestRun {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "status", nullable = false, length = 50)
    private String status;
    @Column(name = "summary", nullable = false)
    private String summary;
    @Column(name = "passed", nullable = false)
    private boolean passed;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @PrePersist void prePersist() { createdAt = Instant.now(); }
}
