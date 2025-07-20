package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.feedbackComment.request.CreateFeedbackCommentDto;
import com.talentradar.assessment_service.dto.feedbackComment.response.FeedbackCommentDto;
import java.util.List;
import java.util.UUID;

public interface FeedbackCommentService {

    List<FeedbackCommentDto> getFeedbackCommentsByFeedbackId(UUID feedbackId);
    FeedbackCommentDto createFeedbackComment(CreateFeedbackCommentDto createDto);
}