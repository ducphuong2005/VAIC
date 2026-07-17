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
@Table(name = "mini_game_onet_mapping")
@Getter
@Setter
public class MiniGameOnetMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mini_game_metric_id", nullable = false)
    private Long miniGameMetricId;

    @Column(name = "element_id", nullable = false, length = 50)
    private String elementId;

    @Column(name = "weight", nullable = false)
    private BigDecimal weight;
}
