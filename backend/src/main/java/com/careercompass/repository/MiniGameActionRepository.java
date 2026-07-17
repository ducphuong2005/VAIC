package com.careercompass.repository;

import com.careercompass.entity.MiniGameAction;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MiniGameActionRepository extends JpaRepository<MiniGameAction, Long> {

    List<MiniGameAction> findBySessionIdOrderByOccurredAtAsc(UUID sessionId);
}
