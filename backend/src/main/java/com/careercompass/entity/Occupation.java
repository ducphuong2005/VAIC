package com.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "occupations")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Occupation {

    @Id
    @Column(name = "onet_code", nullable = false, length = 20)
    private String onetCode;

    @Column(name = "title_vi", nullable = false)
    private String titleVi;

    @Column(name = "title_en", nullable = false)
    private String titleEn;

    @Column(name = "description")
    private String description;

    @Column(name = "career_cluster")
    private String careerCluster;

    @Column(name = "job_zone")
    private Integer jobZone;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }
}
