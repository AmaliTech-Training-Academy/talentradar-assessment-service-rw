package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.feedbackDimension.request.CreateFeedbackDimensionDto;
import com.talentradar.assessment_service.dto.feedbackDimension.response.FeedbackDimensionDto;
import java.util.List;
import java.util.UUID;

public interface FeedbackDimensionService {
    
    List<FeedbackDimensionDto> getFeedbackDimensionsByFeedbackId(UUID feedbackId);
    FeedbackDimensionDto createFeedbackDimension(CreateFeedbackDimensionDto createDto);
    boolean feedbackDimensionExistsForFeedbackAndDimension(UUID feedbackId, UUID dimensionDefinitionId);
}