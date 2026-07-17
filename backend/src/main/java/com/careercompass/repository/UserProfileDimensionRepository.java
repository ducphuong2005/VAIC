package com.careercompass.repository;

import com.careercompass.entity.UserProfileDimension;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileDimensionRepository extends JpaRepository<UserProfileDimension, Long> {

    List<UserProfileDimension> findByUserIdOrderByElementIdAsc(UUID userId);

    Optional<UserProfileDimension> findByUserIdAndElementId(UUID userId, String elementId);
}
