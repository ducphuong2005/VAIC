package com.careercompass.repository;

import com.careercompass.entity.ConversationMessage;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {

    List<ConversationMessage> findBySessionIdOrderByCreatedAtAsc(UUID sessionId);
}
