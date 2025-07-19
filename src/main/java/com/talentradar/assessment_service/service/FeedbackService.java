package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.feedback.request.CreateCompleteFeedbackDto;
import com.talentradar.assessment_service.dto.feedback.response.FeedbackDto;

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
}