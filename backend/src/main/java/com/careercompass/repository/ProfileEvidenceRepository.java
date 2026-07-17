package com.careercompass.repository;

import com.careercompass.entity.ProfileEvidence;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileEvidenceRepository extends JpaRepository<ProfileEvidence, Long> {

    List<ProfileEvidence> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<ProfileEvidence> findByUserIdAndElementId(UUID userId, String elementId);
}
