package com.careercompass.repository;

import com.careercompass.entity.UserExistingSkill;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserExistingSkillRepository extends JpaRepository<UserExistingSkill, Long> {

    List<UserExistingSkill> findByUserIdOrderBySkillNameAsc(UUID userId);

    void deleteByUserId(UUID userId);
}
