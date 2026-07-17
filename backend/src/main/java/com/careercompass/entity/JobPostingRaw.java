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
@Table(name = "job_postings_raw")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobPostingRaw {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "crawl_run_id")
    private Long crawlRunId;

    @Column(name = "source_name", nullable = false, length = 80)
    private String sourceName;

    @Column(name = "external_id", length = 190)
    private String externalId;

    @Column(name = "content_hash", length = 128)
    private String contentHash;

    @Column(name = "raw_payload", nullable = false)
    private String rawPayload;

    @Column(name = "accepted", nullable = false)
    private boolean accepted;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }
}
