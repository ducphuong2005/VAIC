package com.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "occupation_technology_skills")
@Getter
@Setter
public class OccupationTechnologySkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "onet_code", nullable = false, length = 20)
    private String onetCode;

    @Column(name = "technology_skill_id", nullable = false)
    private Long technologySkillId;

    @Column(name = "required_score")
    private BigDecimal requiredScore;
}
