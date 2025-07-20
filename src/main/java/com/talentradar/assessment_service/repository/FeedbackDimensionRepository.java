package com.talentradar.assessment_service.repository;

import com.talentradar.assessment_service.model.FeedbackDimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FeedbackDimensionRepository extends JpaRepository<FeedbackDimension, UUID> {
    List<FeedbackDimension> findByFeedbackId(UUID feedbackId);

    List<FeedbackDimension> findByDimensionDefinitionId(UUID dimensionDefinitionId);

    boolean existsByFeedbackIdAndDimensionDefinitionId(UUID feedbackId, UUID dimensionDefinitionId);
}
