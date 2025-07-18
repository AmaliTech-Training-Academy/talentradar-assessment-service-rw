package com.talentradar.assessment_service.repository;

import com.talentradar.assessment_service.model.Assessment;
import com.talentradar.assessment_service.model.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    boolean existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
            UUID userId,
            SubmissionStatus status,
            LocalDateTime after
    ); //Find latest submission (to prevent re-submission within 30 days)

    Page<Assessment> findAllByUserId(UUID userId, Pageable pageable);

}
