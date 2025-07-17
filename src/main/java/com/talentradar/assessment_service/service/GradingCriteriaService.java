package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.gradingCriteria.CreateGradingCriteriaDto;
import com.talentradar.assessment_service.dto.gradingCriteria.GradingCriteriaDto;
import com.talentradar.assessment_service.dto.gradingCriteria.UpdateGradingCriteriaDto;
import java.util.List;
import java.util.UUID;

public interface GradingCriteriaService {
    
    /**
     * Get all grading criteria
     */
    List<GradingCriteriaDto> getAllGradingCriteria();
    
    /**
     * Get grading criteria by ID
     */
    GradingCriteriaDto getGradingCriteriaById(UUID id);
    
    /**
     * Create new grading criteria
     */
    GradingCriteriaDto createGradingCriteria(CreateGradingCriteriaDto createDto);
    
    /**
     * Update existing grading criteria
     */
    GradingCriteriaDto updateGradingCriteria(UUID id, UpdateGradingCriteriaDto updateDto);
    
    /**
     * Delete grading criteria
     */
    void deleteGradingCriteria(UUID id);
    
    /**
     * Check if grading criteria exists
     */
    boolean gradingCriteriaExists(UUID id);
    
    /**
     * Get multiple grading criteria by IDs (for batch operations)
     */
    List<GradingCriteriaDto> getGradingCriteriaByIds(List<UUID> ids);
}