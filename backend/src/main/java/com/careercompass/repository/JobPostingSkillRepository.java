package com.careercompass.repository;

import com.careercompass.entity.JobPostingSkill;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobPostingSkillRepository extends JpaRepository<JobPostingSkill, Long> {

    void deleteByJobPostingId(Long jobPostingId);

    List<JobPostingSkill> findByJobPostingId(Long jobPostingId);

    @Query("select s.skillName, count(s) from JobPostingSkill s group by s.normalizedSkillName, s.skillName order by count(s) desc")
    List<Object[]> findSkillsInDemand(Pageable pageable);
}
