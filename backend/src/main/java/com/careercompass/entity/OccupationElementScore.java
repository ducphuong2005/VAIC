package com.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "occupation_element_scores")
@Getter
@Setter
public class OccupationElementScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "onet_code", nullable = false, length = 20)
    private String onetCode;

    @Column(name = "element_id", nullable = false, length = 50)
    private String elementId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ElementCategory category;

    @Column(name = "score", nullable = false)
    private BigDecimal score;
}
