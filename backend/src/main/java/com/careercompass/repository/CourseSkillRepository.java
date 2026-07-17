package com.careercompass.repository;

import com.careercompass.entity.CourseSkill;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseSkillRepository extends JpaRepository<CourseSkill, Long> {
    List<CourseSkill> findBySkillNameIgnoreCase(String skillName);
}
