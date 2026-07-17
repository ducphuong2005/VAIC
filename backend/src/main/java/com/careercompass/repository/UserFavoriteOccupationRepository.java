package com.careercompass.repository;

import com.careercompass.entity.UserFavoriteOccupation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFavoriteOccupationRepository extends JpaRepository<UserFavoriteOccupation, Long> {
    List<UserFavoriteOccupation> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<UserFavoriteOccupation> findByUserIdAndOnetCode(UUID userId, String onetCode);
    void deleteByUserIdAndOnetCode(UUID userId, String onetCode);
}
