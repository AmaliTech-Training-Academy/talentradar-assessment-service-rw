package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.dimensionDefinition.request.CreateDimensionDefinitionDto;
import com.talentradar.assessment_service.dto.dimensionDefinition.response.DimensionDefinitionDto;
import com.talentradar.assessment_service.dto.dimensionDefinition.request.UpdateDimensionDefinitionDto;
import java.util.List;
import java.util.UUID;

public interface DimensionDefinitionService {

    List<DimensionDefinitionDto> getAllDimensions();

    DimensionDefinitionDto getDimensionById(UUID id);

    DimensionDefinitionDto createDimension(CreateDimensionDefinitionDto createDto);

    DimensionDefinitionDto updateDimension(UUID id, UpdateDimensionDefinitionDto updateDto);

    void deleteDimension(UUID id);

    boolean dimensionExists(UUID id);

    List<DimensionDefinitionDto> getDimensionsByIds(List<UUID> ids);
}