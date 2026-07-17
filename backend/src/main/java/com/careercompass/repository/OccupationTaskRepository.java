package com.careercompass.repository;

import com.careercompass.entity.OccupationTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OccupationTaskRepository extends JpaRepository<OccupationTask, Long> {

    List<OccupationTask> findByOnetCodeOrderByImportanceScoreDesc(String onetCode);
}
