package com.talentradar.assessment_service.repository;

import com.talentradar.assessment_service.model.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, UUID>, JpaSpecificationExecutor<Feedback> {
    List<Feedback> findByManagerId(UUID managerId);

    List<Feedback> findByDeveloperId(UUID developerId);

    List<Feedback> findByManagerIdAndDeveloperIdOrderByFeedbackVersionDesc(UUID managerId, UUID developerId);

    Optional<Feedback> findTopByManagerIdAndDeveloperIdOrderByFeedbackVersionDesc(UUID managerId, UUID developerId);
}
