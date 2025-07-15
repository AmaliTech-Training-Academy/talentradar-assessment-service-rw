package com.talentradar.assessment_service.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.*;
import java.util.*;

@Entity
@Table(name = "feedback")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Feedback {

    @Id
    private UUID id;

    @Column(name = "manager_id", nullable = false)
    private UUID managerId;

    @Column(name = "developer_id", nullable = false)
    private UUID developerId;

    @Column(name = "feedback_version", nullable = false)
    private int feedbackVersion;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL)
    private List<FeedbackDimension> dimensions;

    @OneToMany(mappedBy = "feedback", cascade = CascadeType.ALL)
    private List<FeedbackComment> feedbackComments;
}
