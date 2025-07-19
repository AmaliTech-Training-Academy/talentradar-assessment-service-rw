package com.talentradar.assessment_service.repository;

import com.talentradar.assessment_service.model.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID> {
    List<Feedback> findByManagerId(UUID managerId);

    List<Feedback> findByDeveloperId(UUID developerId);

    List<Feedback> findByManagerIdAndDeveloperIdOrderByFeedbackVersionDesc(UUID managerId, UUID developerId);

    Optional<Feedback> findTopByManagerIdAndDeveloperIdOrderByFeedbackVersionDesc(UUID managerId, UUID developerId);
    @Query("SELECT f FROM Feedback f WHERE " +
            "(:managerId IS NULL OR f.managerId = :managerId) AND " +
            "(:developerId IS NULL OR f.developerId = :developerId) AND " +
            "(:feedbackVersion IS NULL OR f.feedbackVersion = :feedbackVersion) AND " +
            "(:createdAfter IS NULL OR f.createdAt >= :createdAfter) AND " +
            "(:createdBefore IS NULL OR f.createdAt <= :createdBefore)")
    Page<Feedback> searchFeedbacks(@Param("managerId") UUID managerId,
                                   @Param("developerId") UUID developerId,
                                   @Param("feedbackVersion") Integer feedbackVersion,
                                   @Param("createdAfter") LocalDateTime createdAfter,
                                   @Param("createdBefore") LocalDateTime createdBefore,
                                   Pageable pageable);

}
