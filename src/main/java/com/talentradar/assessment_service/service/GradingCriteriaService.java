package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.gradingCriteria.request.CreateGradingCriteriaDto;
import com.talentradar.assessment_service.dto.gradingCriteria.response.GradingCriteriaDto;
import com.talentradar.assessment_service.dto.gradingCriteria.request.UpdateGradingCriteriaDto;
import java.util.List;
import java.util.UUID;

public interface GradingCriteriaService {
    
    List<GradingCriteriaDto> getAllGradingCriteria();
    
    GradingCriteriaDto getGradingCriteriaById(UUID id);
    
    GradingCriteriaDto createGradingCriteria(CreateGradingCriteriaDto createDto);
    
    GradingCriteriaDto updateGradingCriteria(UUID id, UpdateGradingCriteriaDto updateDto);
    
    void deleteGradingCriteria(UUID id);
    
    boolean gradingCriteriaExists(UUID id);
    
    List<GradingCriteriaDto> getGradingCriteriaByIds(List<UUID> ids);
}