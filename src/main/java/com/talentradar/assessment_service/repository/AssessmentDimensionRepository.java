package com.talentradar.assessment_service.repository;

import com.talentradar.assessment_service.model.AssessmentDimension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentDimensionRepository extends JpaRepository<AssessmentDimension, UUID> {

    List<AssessmentDimension> findByAssessmentId(UUID assessmentId);
}
