package com.careercompass.repository;

import com.careercompass.entity.ConversationSession;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationSessionRepository extends JpaRepository<ConversationSession, UUID> {

    List<ConversationSession> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    Optional<ConversationSession> findByIdAndUserId(UUID id, UUID userId);
}
