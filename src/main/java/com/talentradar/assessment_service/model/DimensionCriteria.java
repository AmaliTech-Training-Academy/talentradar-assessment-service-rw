package com.talentradar.assessment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "dimension_criteria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionCriteria {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "dimension_definition_id")
    private DimensionDefinition dimensionDefinition;

    @ManyToOne
    @JoinColumn(name = "criteria_id")
    private GradingCriteria criteria;
}

