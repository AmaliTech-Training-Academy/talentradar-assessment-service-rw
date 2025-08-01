package com.talentradar.assessment_service.model;



import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "dimension_definition")
@Getter
@Setter
@EqualsAndHashCode(exclude = {"gradingCriteriaSet", "assessmentDimensions", "feedbackDimensions"})
@ToString(exclude = {"gradingCriteriaSet", "assessmentDimensions", "feedbackDimensions"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "dimension_name", nullable = false)
    private String dimensionName;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal weight;

    @OneToMany(mappedBy = "dimensionDefinition", cascade = CascadeType.ALL)
    private List<AssessmentDimension> assessmentDimensions;

    @OneToMany(mappedBy = "dimensionDefinition", cascade = CascadeType.ALL)
    private List<FeedbackDimension> feedbackDimensions;

    @Builder.Default
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "dimension_grading_criteria",
            joinColumns = @JoinColumn(name = "dimension_definition_id"),
            inverseJoinColumns = @JoinColumn(name = "grading_criteria_id")
    )
    private Set<GradingCriteria> gradingCriteriaSet = new HashSet<>();
}
