package com.talentradar.assessment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Entity
@Table(name = "grading_criteria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradingCriteria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "criteria_name", nullable = false)
    private String criteriaName;

    @ManyToMany(mappedBy = "gradingCriteriaSet")
    private Set<DimensionDefinition> dimensionDefinitions = new HashSet<>();
}

