package com.careercompass.repository;

import com.careercompass.entity.RelatedOccupation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelatedOccupationRepository extends JpaRepository<RelatedOccupation, Long> {

    List<RelatedOccupation> findByOnetCode(String onetCode);
}
