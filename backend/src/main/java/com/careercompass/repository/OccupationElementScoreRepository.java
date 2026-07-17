package com.careercompass.repository;

import com.careercompass.entity.ElementCategory;
import com.careercompass.entity.OccupationElementScore;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OccupationElementScoreRepository extends JpaRepository<OccupationElementScore, Long> {

    List<OccupationElementScore> findTop5ByOnetCodeAndCategoryOrderByScoreDesc(String onetCode, ElementCategory category);

    List<OccupationElementScore> findByOnetCodeAndCategory(String onetCode, ElementCategory category);
}
