package com.careercompass.repository;

import com.careercompass.entity.JobPosting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    Optional<JobPosting> findBySourceNameAndExternalId(String sourceName, String externalId);

    boolean existsByContentHash(String contentHash);

    long countByOnetCodeAndRegionAndActiveTrue(String onetCode, String region);

    List<JobPosting> findByActiveTrueAndOnetCodeIsNotNull();

    @Query("select distinct j.region from JobPosting j where j.active = true and j.region is not null order by j.region")
    List<String> findActiveRegions();

    @Query("select j.onetCode, count(j) from JobPosting j where j.active = true and j.onetCode is not null group by j.onetCode order by count(j) desc")
    List<Object[]> findTrendingOnetCodes(Pageable pageable);
}
