package com.talentradar.assessment_service.service.impl;

import com.talentradar.assessment_service.dto.dimensionDefinition.CreateDimensionDefinitionDto;
import com.talentradar.assessment_service.dto.dimensionDefinition.DimensionDefinitionDto;
import com.talentradar.assessment_service.dto.dimensionDefinition.UpdateDimensionDefinitionDto;
import com.talentradar.assessment_service.dto.gradingCriteria.GradingCriteriaDto;
import com.talentradar.assessment_service.exception.DimensionDefinitionNotFoundException;
import com.talentradar.assessment_service.exception.GradingCriteriaNotFoundException;
import com.talentradar.assessment_service.model.DimensionDefinition;
import com.talentradar.assessment_service.model.GradingCriteria;
import com.talentradar.assessment_service.repository.DimensionDefinitionRepository;
import com.talentradar.assessment_service.repository.GradingCriteriaRepository;
import com.talentradar.assessment_service.service.DimensionDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DimensionDefinitionServiceImpl implements DimensionDefinitionService {

    private final DimensionDefinitionRepository dimensionDefinitionRepository;
    private final GradingCriteriaRepository gradingCriteriaRepository;

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
        Set<GradingCriteria> gradingCriteria = new HashSet<>();
        if (createDto.getGradingCriteriaIds() != null && !createDto.getGradingCriteriaIds().isEmpty()) {
            gradingCriteria = validateAndFetchGradingCriteria(createDto.getGradingCriteriaIds());
        }

        DimensionDefinition dimension = DimensionDefinition.builder()
                .dimensionName(createDto.getDimensionName())
                .description(createDto.getDescription())
                .weight(createDto.getWeight())
                .gradingCriteriaSet(gradingCriteria)
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
        if (updateDto.getGradingCriteriaIds() != null) {
            Set<GradingCriteria> gradingCriteria = validateAndFetchGradingCriteria(updateDto.getGradingCriteriaIds());
            dimension.setGradingCriteriaSet(gradingCriteria);
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

    private Set<GradingCriteria> validateAndFetchGradingCriteria(Set<UUID> criteriaIds) {
        Set<GradingCriteria> gradingCriteria = new HashSet<>(gradingCriteriaRepository.findAllById(criteriaIds));

        if (gradingCriteria.size() != criteriaIds.size()) {
            Set<UUID> foundIds = gradingCriteria.stream()
                    .map(GradingCriteria::getId)
                    .collect(Collectors.toSet());
            Set<UUID> missingIds = criteriaIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());
            throw new GradingCriteriaNotFoundException("Grading criteria not found with ids: " + missingIds);
        }

        return gradingCriteria;
    }

    private DimensionDefinitionDto mapToDto(DimensionDefinition dimension) {
        Set<GradingCriteriaDto> gradingCriteriaDto = dimension.getGradingCriteriaSet() != null ?
                dimension.getGradingCriteriaSet().stream()
                        .map(this::mapGradingCriteriaToDto)
                        .collect(Collectors.toSet()) : new HashSet<>();

        return DimensionDefinitionDto.builder()
                .id(dimension.getId())
                .dimensionName(dimension.getDimensionName())
                .description(dimension.getDescription())
                .weight(dimension.getWeight())
                .gradingCriteria(gradingCriteriaDto)
                .build();
    }

    private GradingCriteriaDto mapGradingCriteriaToDto(GradingCriteria gradingCriteria) {
        return GradingCriteriaDto.builder()
                .id(gradingCriteria.getId())
                .criteriaName(gradingCriteria.getCriteriaName())
                .build();
    }
}