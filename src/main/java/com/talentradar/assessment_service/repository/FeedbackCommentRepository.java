package com.talentradar.assessment_service.repository;

import com.talentradar.assessment_service.model.Feedback;
import com.talentradar.assessment_service.model.FeedbackComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackCommentRepository extends JpaRepository<FeedbackComment, UUID> {
    Optional<FeedbackComment> findByFeedback(Feedback uuid);

    List<FeedbackComment> findByFeedbackId(FeedbackComment feedbackComment);
}
