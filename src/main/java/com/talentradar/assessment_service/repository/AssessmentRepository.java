package com.talentradar.assessment_service.repository;

import com.talentradar.assessment_service.model.Assessment;
import com.talentradar.assessment_service.model.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, UUID> {

    boolean existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
            UUID userId,
            SubmissionStatus status,
            LocalDateTime after
    ); //Find latest submission (to prevent re-submission within 30 days)

    @Query("SELECT a FROM Assessment a " +
            "LEFT JOIN FETCH a.dimensions d " +
            "LEFT JOIN FETCH d.dimensionDefinition " +
            "WHERE a.userId = :userId " +
            "ORDER BY a.createdAt DESC")
    Page<Assessment> findAllByUserIdWithDimensions(@Param("userId") UUID userId, Pageable pageable);
    @Query("SELECT a FROM Assessment a " +
            "LEFT JOIN FETCH a.dimensions d " +
            "LEFT JOIN FETCH d.dimensionDefinition " +
            "WHERE a.userId = :userId " +
            "AND a.submissionStatus = 'SUBMITTED' " +
            "ORDER BY a.createdAt DESC")
    Optional<Assessment> findLatestSubmittedAssessmentByUserId(@Param("userId") UUID userId);

}
