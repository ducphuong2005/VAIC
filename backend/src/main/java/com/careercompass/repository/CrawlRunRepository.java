package com.careercompass.repository;

import com.careercompass.entity.CrawlRun;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlRunRepository extends JpaRepository<CrawlRun, Long> {
}
