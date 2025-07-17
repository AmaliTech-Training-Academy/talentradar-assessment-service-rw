package com.talentradar.assessment_service.model;


import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "grading_criteria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(exclude = {"dimensionDefinitions"})
@ToString(exclude = {"dimensionDefinitions"})
public class GradingCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "criteria_name", nullable = false)
    private String criteriaName;

    @ManyToMany(mappedBy = "gradingCriteriaSet")
    private Set<DimensionDefinition> dimensionDefinitions = new HashSet<>();
}

