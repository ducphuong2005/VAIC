package com.careercompass.repository;

import com.careercompass.entity.MiniGameSession;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiniGameSessionRepository extends JpaRepository<MiniGameSession, UUID> {
}
