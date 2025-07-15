package com.talentradar.assessment_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feedback_dimension")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackDimension {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "feedback_id")
    private Feedback feedback;

    @ManyToOne
    @JoinColumn(name = "dimension_definition_id")
    private DimensionDefinition dimensionDefinition;

    private int rating;

    @Column(nullable = false)
    private String comment;

    @CreationTimestamp
    private LocalDateTime createdAt;
}

