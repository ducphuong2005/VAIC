package com.careercompass.repository;

import com.careercompass.entity.LlmCall;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LlmCallRepository extends JpaRepository<LlmCall, UUID> {
}
