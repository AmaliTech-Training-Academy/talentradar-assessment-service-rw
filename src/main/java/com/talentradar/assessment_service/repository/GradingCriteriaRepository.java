package com.talentradar.assessment_service.repository;

import com.talentradar.assessment_service.model.GradingCriteria;
import io.micrometer.core.instrument.config.validate.Validated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GradingCriteriaRepository extends JpaRepository<GradingCriteria, UUID> {
    Optional<GradingCriteria> findByCriteriaName(String criteriaName);
}
