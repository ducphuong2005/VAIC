package com.careercompass.repository;

import com.careercompass.entity.OccupationTechnologySkill;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OccupationTechnologySkillRepository extends JpaRepository<OccupationTechnologySkill, Long> {

    List<OccupationTechnologySkill> findByOnetCodeOrderByRequiredScoreDesc(String onetCode);
}
