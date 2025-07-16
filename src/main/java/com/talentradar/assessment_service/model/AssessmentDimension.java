package com.talentradar.assessment_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "assessment_dimension")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentDimension {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "assessment_id")
    private Assessment assessment;

    @ManyToOne
    @JoinColumn(name = "dimension_definition_id")
    private DimensionDefinition dimensionDefinition;

    private int rating;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

