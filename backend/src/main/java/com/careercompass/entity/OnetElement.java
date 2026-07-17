package com.careercompass.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "onet_elements")
@Getter
@Setter
public class OnetElement {

    @Id
    @Column(name = "element_id", nullable = false, length = 50)
    private String elementId;

    @Column(name = "element_name", nullable = false)
    private String elementName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ElementCategory category;

    @Column(name = "description")
    private String description;
}
