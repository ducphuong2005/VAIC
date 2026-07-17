package com.careercompass.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name = "user_favorite_courses")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserFavoriteCourse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @JdbcTypeCode(SqlTypes.CHAR) @Column(name = "user_id", nullable = false, length = 36) private UUID userId;
    @Column(name = "course_id", nullable = false) private Long courseId;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @PrePersist void prePersist() { createdAt = Instant.now(); }
}
