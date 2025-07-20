package com.talentradar.assessment_service.service.impl;

import com.talentradar.assessment_service.dto.dimensionDefinition.response.DimensionDefinitionDto;
import com.talentradar.assessment_service.dto.feedbackDimension.request.CreateFeedbackDimensionDto;
import com.talentradar.assessment_service.dto.feedbackDimension.response.FeedbackDimensionDto;
import com.talentradar.assessment_service.dto.gradingCriteria.response.GradingCriteriaDto;
import com.talentradar.assessment_service.exception.DimensionDefinitionNotFoundException;
import com.talentradar.assessment_service.exception.FeedbackDimensionNotFoundException;
import com.talentradar.assessment_service.exception.FeedbackNotFoundException;
import com.talentradar.assessment_service.model.DimensionDefinition;
import com.talentradar.assessment_service.model.Feedback;
import com.talentradar.assessment_service.model.FeedbackDimension;
import com.talentradar.assessment_service.repository.DimensionDefinitionRepository;
import com.talentradar.assessment_service.repository.FeedbackDimensionRepository;
import com.talentradar.assessment_service.repository.FeedbackRepository;
import com.talentradar.assessment_service.service.FeedbackDimensionService;
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
public class FeedbackDimensionServiceImpl implements FeedbackDimensionService {
    
    private final FeedbackDimensionRepository feedbackDimensionRepository;
    private final FeedbackRepository feedbackRepository;
    private final DimensionDefinitionRepository dimensionDefinitionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackDimensionDto> getFeedbackDimensionsByFeedbackId(UUID feedbackId) {
        return feedbackDimensionRepository.findByFeedbackId(feedbackId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public FeedbackDimensionDto createFeedbackDimension(CreateFeedbackDimensionDto createDto) {
        // Validate feedback exists
        Feedback feedback = feedbackRepository.findById(createDto.getFeedbackId())
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found with id: " + createDto.getFeedbackId()));
        
        // Validate dimension definition exists
        DimensionDefinition dimensionDefinition = dimensionDefinitionRepository.findById(createDto.getDimensionDefinitionId())
                .orElseThrow(() -> new DimensionDefinitionNotFoundException("Dimension definition not found with id: " + createDto.getDimensionDefinitionId()));
        
        // Check if feedback dimension already exists for this feedback and dimension
        if (feedbackDimensionExistsForFeedbackAndDimension(createDto.getFeedbackId(), createDto.getDimensionDefinitionId())) {
            throw new IllegalArgumentException("Feedback dimension already exists for feedback ID: " + 
                createDto.getFeedbackId() + " and dimension ID: " + createDto.getDimensionDefinitionId());
        }
        
        FeedbackDimension feedbackDimension = FeedbackDimension.builder()
                .feedback(feedback)
                .dimensionDefinition(dimensionDefinition)
                .rating(createDto.getRating())
                .comment(createDto.getComment())
                .build();
        
        FeedbackDimension savedFeedbackDimension = feedbackDimensionRepository.save(feedbackDimension);
        return mapToDto(savedFeedbackDimension);
    }
    




    @Override
    @Transactional(readOnly = true)
    public boolean feedbackDimensionExistsForFeedbackAndDimension(UUID feedbackId, UUID dimensionDefinitionId) {
        return feedbackDimensionRepository.existsByFeedbackIdAndDimensionDefinitionId(feedbackId, dimensionDefinitionId);
    }
    
    private FeedbackDimensionDto mapToDto(FeedbackDimension feedbackDimension) {
        DimensionDefinitionDto dimensionDefinitionDto = mapDimensionDefinitionToDto(feedbackDimension.getDimensionDefinition());
        
        return FeedbackDimensionDto.builder()
                .id(feedbackDimension.getId())
                .feedbackId(feedbackDimension.getFeedback().getId())
                .dimensionDefinition(dimensionDefinitionDto)
                .rating(feedbackDimension.getRating())
                .comment(feedbackDimension.getComment())
                .createdAt(feedbackDimension.getCreatedAt())
                .build();
    }
    
    private DimensionDefinitionDto mapDimensionDefinitionToDto(DimensionDefinition dimensionDefinition) {
        Set<GradingCriteriaDto> gradingCriteriaDto = dimensionDefinition.getGradingCriteriaSet() != null ?
                dimensionDefinition.getGradingCriteriaSet().stream()
                        .map(this::mapGradingCriteriaToDto)
                        .collect(Collectors.toSet()) : new HashSet<>();
        
        return DimensionDefinitionDto.builder()
                .id(dimensionDefinition.getId())
                .dimensionName(dimensionDefinition.getDimensionName())
                .description(dimensionDefinition.getDescription())
                .weight(dimensionDefinition.getWeight())
                .gradingCriteria(gradingCriteriaDto)
                .build();
    }
    
    private GradingCriteriaDto mapGradingCriteriaToDto(com.talentradar.assessment_service.model.GradingCriteria gradingCriteria) {
        return GradingCriteriaDto.builder()
                .id(gradingCriteria.getId())
                .criteriaName(gradingCriteria.getCriteriaName())
                .build();
    }
}