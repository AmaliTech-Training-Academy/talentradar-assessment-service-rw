package com.talentradar.assessment_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "feedback_comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackComment {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "feedback_id")
    private Feedback feedback;

    @ManyToOne
    @JoinColumn(name = "comment_id")
    private Comment comment;

    @Column(name = "feedback_comment_body", nullable = false)
    private String feedbackCommentBody;
}

