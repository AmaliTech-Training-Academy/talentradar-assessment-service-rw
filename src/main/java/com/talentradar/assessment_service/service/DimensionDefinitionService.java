package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.dimensionDefinition.CreateDimensionDefinitionDto;
import com.talentradar.assessment_service.dto.dimensionDefinition.DimensionDefinitionDto;
import com.talentradar.assessment_service.dto.dimensionDefinition.UpdateDimensionDefinitionDto;
import java.util.List;
import java.util.UUID;

public interface DimensionDefinitionService {

    /**
     * Get all dimension definitions
     */
    List<DimensionDefinitionDto> getAllDimensions();

    /**
     * Get dimension definition by ID
     */
    DimensionDefinitionDto getDimensionById(UUID id);

    /**
     * Create new dimension definition
     */
    DimensionDefinitionDto createDimension(CreateDimensionDefinitionDto createDto);

    /**
     * Update existing dimension definition
     */
    DimensionDefinitionDto updateDimension(UUID id, UpdateDimensionDefinitionDto updateDto);

    /**
     * Delete dimension definition
     */
    void deleteDimension(UUID id);

    /**
     * Check if dimension exists
     */
    boolean dimensionExists(UUID id);

    /**
     * Get multiple dimensions by IDs (for batch operations)
     */
    List<DimensionDefinitionDto> getDimensionsByIds(List<UUID> ids);
}