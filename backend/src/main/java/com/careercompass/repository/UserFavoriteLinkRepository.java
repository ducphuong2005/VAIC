package com.careercompass.repository;

import com.careercompass.entity.UserFavoriteLink;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFavoriteLinkRepository extends JpaRepository<UserFavoriteLink, Long> {
    List<UserFavoriteLink> findByUserIdOrderByCreatedAtDesc(UUID userId);
    Optional<UserFavoriteLink> findByUserIdAndUrl(UUID userId, String url);
    void deleteByUserIdAndId(UUID userId, Long id);
}
