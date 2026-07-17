package com.careercompass.repository;

import com.careercompass.entity.FairnessTestRun;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FairnessTestRunRepository extends JpaRepository<FairnessTestRun, Long> {
    List<FairnessTestRun> findAllByOrderByCreatedAtDesc();
}
