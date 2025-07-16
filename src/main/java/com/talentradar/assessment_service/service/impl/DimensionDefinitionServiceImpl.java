package com.talentradar.assessment_service.service.impl;

import com.talentradar.assessment_service.dto.dimensionDefinition.CreateDimensionDefinitionDto;
import com.talentradar.assessment_service.dto.dimensionDefinition.DimensionDefinitionDto;
import com.talentradar.assessment_service.dto.dimensionDefinition.UpdateDimensionDefinitionDto;
import com.talentradar.assessment_service.exception.DimensionDefinitionNotFoundException;
import com.talentradar.assessment_service.model.DimensionDefinition;
import com.talentradar.assessment_service.repository.DimensionDefinitionRepository;
import com.talentradar.assessment_service.service.DimensionDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DimensionDefinitionServiceImpl implements DimensionDefinitionService {

    private final DimensionDefinitionRepository dimensionDefinitionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DimensionDefinitionDto> getAllDimensions() {
        return dimensionDefinitionRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DimensionDefinitionDto getDimensionById(UUID id) {
        DimensionDefinition dimension = dimensionDefinitionRepository.findById(id)
                .orElseThrow(() -> new DimensionDefinitionNotFoundException("Dimension not found with id: " + id));
        return mapToDto(dimension);
    }

    @Override
    public DimensionDefinitionDto createDimension(CreateDimensionDefinitionDto createDto) {
        DimensionDefinition dimension = DimensionDefinition.builder()
                .dimensionName(createDto.getDimensionName())
                .description(createDto.getDescription())
                .weight(createDto.getWeight())
                .build();

        DimensionDefinition savedDimension = dimensionDefinitionRepository.save(dimension);
        return mapToDto(savedDimension);
    }

    @Override
    public DimensionDefinitionDto updateDimension(UUID id, UpdateDimensionDefinitionDto updateDto) {
        DimensionDefinition dimension = dimensionDefinitionRepository.findById(id)
                .orElseThrow(() -> new DimensionDefinitionNotFoundException("Dimension not found with id: " + id));

        if (updateDto.getDimensionName() != null) {
            dimension.setDimensionName(updateDto.getDimensionName());
        }
        if (updateDto.getDescription() != null) {
            dimension.setDescription(updateDto.getDescription());
        }
        if (updateDto.getWeight() != null) {
            dimension.setWeight(updateDto.getWeight());
        }

        DimensionDefinition updatedDimension = dimensionDefinitionRepository.save(dimension);
        return mapToDto(updatedDimension);
    }

    @Override
    public void deleteDimension(UUID id) {
        DimensionDefinition dimension = dimensionDefinitionRepository.findById(id)
                .orElseThrow(() -> new DimensionDefinitionNotFoundException("Dimension not found with id: " + id));
        dimensionDefinitionRepository.delete(dimension);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean dimensionExists(UUID id) {
        return dimensionDefinitionRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DimensionDefinitionDto> getDimensionsByIds(List<UUID> ids) {
        return dimensionDefinitionRepository.findAllById(ids)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private DimensionDefinitionDto mapToDto(DimensionDefinition dimension) {
        return DimensionDefinitionDto.builder()
                .id(dimension.getId())
                .dimensionName(dimension.getDimensionName())
                .description(dimension.getDescription())
                .weight(dimension.getWeight())
                .build();
    }
}