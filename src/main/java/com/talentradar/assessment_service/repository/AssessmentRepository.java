package com.talentradar.assessment_service.repository;

import com.talentradar.assessment_service.model.Assessment;
import com.talentradar.assessment_service.model.SubmissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    boolean existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
            UUID userId,
            SubmissionStatus status,
            LocalDateTime after
    ); //Find latest submission (to prevent re-submission within 30 days)

}
