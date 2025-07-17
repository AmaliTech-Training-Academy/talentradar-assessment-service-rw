package com.talentradar.assessment_service.service.impl;

import com.talentradar.assessment_service.dto.gradingCriteria.request.CreateGradingCriteriaDto;
import com.talentradar.assessment_service.dto.gradingCriteria.response.GradingCriteriaDto;
import com.talentradar.assessment_service.dto.gradingCriteria.request.UpdateGradingCriteriaDto;
import com.talentradar.assessment_service.exception.GradingCriteriaNotFoundException;
import com.talentradar.assessment_service.model.GradingCriteria;
import com.talentradar.assessment_service.repository.GradingCriteriaRepository;
import com.talentradar.assessment_service.service.GradingCriteriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class GradingCriteriaServiceImpl implements GradingCriteriaService {
    
    private final GradingCriteriaRepository gradingCriteriaRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<GradingCriteriaDto> getAllGradingCriteria() {
        return gradingCriteriaRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public GradingCriteriaDto getGradingCriteriaById(UUID id) {
        GradingCriteria gradingCriteria = gradingCriteriaRepository.findById(id)
                .orElseThrow(() -> new GradingCriteriaNotFoundException("Grading criteria not found with id: " + id));
        return mapToDto(gradingCriteria);
    }
    
    @Override
    public GradingCriteriaDto createGradingCriteria(CreateGradingCriteriaDto createDto) {
        GradingCriteria gradingCriteria = GradingCriteria.builder()
                .criteriaName(createDto.getCriteriaName())
                .dimensionDefinitions(new HashSet<>())
                .build();
        
        GradingCriteria savedGradingCriteria = gradingCriteriaRepository.save(gradingCriteria);
        return mapToDto(savedGradingCriteria);
    }
    
    @Override
    public GradingCriteriaDto updateGradingCriteria(UUID id, UpdateGradingCriteriaDto updateDto) {
        GradingCriteria gradingCriteria = gradingCriteriaRepository.findById(id)
                .orElseThrow(() -> new GradingCriteriaNotFoundException("Grading criteria not found with id: " + id));
        
        if (updateDto.getCriteriaName() != null) {
            gradingCriteria.setCriteriaName(updateDto.getCriteriaName());
        }
        
        GradingCriteria updatedGradingCriteria = gradingCriteriaRepository.save(gradingCriteria);
        return mapToDto(updatedGradingCriteria);
    }
    
    @Override
    public void deleteGradingCriteria(UUID id) {
        GradingCriteria gradingCriteria = gradingCriteriaRepository.findById(id)
                .orElseThrow(() -> new GradingCriteriaNotFoundException("Grading criteria not found with id: " + id));
        gradingCriteriaRepository.delete(gradingCriteria);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean gradingCriteriaExists(UUID id) {
        return gradingCriteriaRepository.existsById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<GradingCriteriaDto> getGradingCriteriaByIds(List<UUID> ids) {
        return gradingCriteriaRepository.findAllById(ids)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    private GradingCriteriaDto mapToDto(GradingCriteria gradingCriteria) {
        return GradingCriteriaDto.builder()
                .id(gradingCriteria.getId())
                .criteriaName(gradingCriteria.getCriteriaName())
                .build();
    }
}