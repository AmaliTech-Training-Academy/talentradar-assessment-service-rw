package com.talentradar.assessment_service.service.impl;

import com.talentradar.assessment_service.dto.comment.response.CommentDto;
import com.talentradar.assessment_service.dto.feedbackComment.request.CreateFeedbackCommentDto;
import com.talentradar.assessment_service.dto.feedbackComment.response.FeedbackCommentDto;
import com.talentradar.assessment_service.exception.CommentNotFoundException;
import com.talentradar.assessment_service.exception.FeedbackCommentNotFoundException;
import com.talentradar.assessment_service.exception.FeedbackNotFoundException;
import com.talentradar.assessment_service.model.Comment;
import com.talentradar.assessment_service.model.Feedback;
import com.talentradar.assessment_service.model.FeedbackComment;
import com.talentradar.assessment_service.repository.CommentRepository;
import com.talentradar.assessment_service.repository.FeedbackCommentRepository;
import com.talentradar.assessment_service.repository.FeedbackRepository;
import com.talentradar.assessment_service.service.FeedbackCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FeedbackCommentServiceImpl implements FeedbackCommentService {

    private final FeedbackCommentRepository feedbackCommentRepository;
    private final FeedbackRepository feedbackRepository;
    private final CommentRepository commentRepository;



    @Override
    @Transactional(readOnly = true)
    public List<FeedbackCommentDto> getFeedbackCommentsByFeedbackId(UUID feedbackId) {
        return feedbackCommentRepository.findByFeedbackId(FeedbackComment.builder().id(feedbackId).build())
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public FeedbackCommentDto createFeedbackComment(CreateFeedbackCommentDto createDto) {
        Feedback feedback = feedbackRepository.findById(createDto.getFeedbackId())
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found with id: " + createDto.getFeedbackId()));

        Comment comment = commentRepository.findById(createDto.getCommentId())
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + createDto.getCommentId()));

        FeedbackComment feedbackComment = FeedbackComment.builder()
                .feedback(feedback)
                .comment(comment)
                .feedbackCommentBody(createDto.getFeedbackCommentBody())
                .build();

        FeedbackComment savedFeedbackComment = feedbackCommentRepository.save(feedbackComment);
        return mapToDto(savedFeedbackComment);
    }

    private FeedbackCommentDto mapToDto(FeedbackComment feedbackComment) {
        CommentDto commentDto = CommentDto.builder()
                .id(feedbackComment.getComment().getId())
                .commentTitle(feedbackComment.getComment().getCommentTitle())
                .build();

        return FeedbackCommentDto.builder()
                .id(feedbackComment.getId())
                .feedbackId(feedbackComment.getFeedback().getId())
                .comment(commentDto)
                .feedbackCommentBody(feedbackComment.getFeedbackCommentBody())
                .build();
    }
}