package com.careercompass.repository;

import com.careercompass.entity.Occupation;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OccupationRepository extends JpaRepository<Occupation, String> {

    @Query("""
            select distinct o
            from Occupation o
            where o.active = true
              and (:cluster is null or o.careerCluster = :cluster)
              and (
                :q is null
                or lower(o.titleVi) like lower(concat('%', :q, '%'))
                or lower(o.titleEn) like lower(concat('%', :q, '%'))
                or exists (
                    select 1 from OccupationAlias a
                    where a.onetCode = o.onetCode
                      and lower(a.alias) like lower(concat('%', :q, '%'))
                )
                or exists (
                    select 1 from OccupationTechnologySkill ots
                    where ots.onetCode = o.onetCode
                      and exists (
                        select 1 from TechnologySkill ts
                        where ts.id = ots.technologySkillId
                          and lower(ts.skillName) like lower(concat('%', :q, '%'))
                      )
                )
              )
            """)
    Page<Occupation> search(@Param("q") String q, @Param("cluster") String cluster, Pageable pageable);

    List<Occupation> findByActiveTrueOrderByTitleViAsc();

    @Query("select distinct o.careerCluster from Occupation o where o.active = true and o.careerCluster is not null order by o.careerCluster")
    List<String> findCareerClusters();
}
