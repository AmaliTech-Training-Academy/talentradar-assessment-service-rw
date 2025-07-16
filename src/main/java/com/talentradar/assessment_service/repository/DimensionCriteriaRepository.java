package com.talentradar.assessment_service.repository;

import com.talentradar.assessment_service.model.DimensionCriteria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DimensionCriteriaRepository extends JpaRepository<DimensionCriteria, UUID> {
}
