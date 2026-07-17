package com.careercompass.repository;

import com.careercompass.entity.UserFavoriteCourse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFavoriteCourseRepository extends JpaRepository<UserFavoriteCourse, Long> {
    List<UserFavoriteCourse> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<UserFavoriteCourse> findByUserIdAndCourseId(UUID userId, Long courseId);
    void deleteByUserIdAndCourseId(UUID userId, Long courseId);
}
