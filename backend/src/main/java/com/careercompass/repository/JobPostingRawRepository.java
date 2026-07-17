package com.careercompass.repository;

import com.careercompass.entity.JobPostingRaw;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingRawRepository extends JpaRepository<JobPostingRaw, Long> {
}
