package com.careercompass.repository;

import com.careercompass.entity.MiniGameSession;
import com.careercompass.entity.SessionStatus;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiniGameSessionRepository extends JpaRepository<MiniGameSession, UUID> {
    Optional<MiniGameSession> findFirstByUserIdAndStatusOrderByCompletedAtDesc(UUID userId, SessionStatus status);
}
