package com.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "related_occupations")
@Getter
@Setter
public class RelatedOccupation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "onet_code", nullable = false, length = 20)
    private String onetCode;

    @Column(name = "related_onet_code", nullable = false, length = 20)
    private String relatedOnetCode;

    @Column(name = "relation_type", nullable = false, length = 50)
    private String relationType;
}
