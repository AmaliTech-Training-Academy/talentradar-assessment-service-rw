package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.assessment.response.PaginatedResponseDTO;
import com.talentradar.assessment_service.dto.feedback.request.CreateCompleteFeedbackDto;
import com.talentradar.assessment_service.dto.feedback.request.FeedbackSearchCriteria;
import com.talentradar.assessment_service.dto.feedback.response.FeedbackDto;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface FeedbackService {

    List<FeedbackDto> getAllFeedback();
    FeedbackDto getFeedbackById(UUID id);
    List<FeedbackDto> getFeedbackByManagerId(UUID managerId);
    List<FeedbackDto> getFeedbackByDeveloperId(UUID developerId);
    List<FeedbackDto> getFeedbackByManagerAndDeveloper(UUID managerId, UUID developerId);
    FeedbackDto getLatestFeedbackVersion(UUID managerId, UUID developerId);
    void deleteFeedback(UUID id);
    boolean feedbackExists(UUID id);
    List<FeedbackDto> getFeedbackByIds(List<UUID> ids);
    FeedbackDto createNewFeedbackVersion(UUID managerId, UUID developerId);
    FeedbackDto getFeedbackWithDetails(UUID id);
    FeedbackDto createCompleteFeedback(CreateCompleteFeedbackDto createDto);
    FeedbackDto getCompleteFeedback(UUID id);
    FeedbackDto updateCompleteFeedback(UUID id, CreateCompleteFeedbackDto updateDto);

    @Transactional(readOnly = true)
    PaginatedResponseDTO<FeedbackDto> searchFeedbacks(FeedbackSearchCriteria criteria, Pageable pageable);
}