package com.talentradar.assessment_service.controller;

import com.talentradar.assessment_service.dto.api.ApiResponse;
import com.talentradar.assessment_service.dto.assessment.response.PaginatedResponseDTO;
import com.talentradar.assessment_service.dto.comment.request.CreateCommentDto;
import com.talentradar.assessment_service.dto.comment.response.CommentDto;
import com.talentradar.assessment_service.dto.feedback.request.CreateCompleteFeedbackDto;
import com.talentradar.assessment_service.dto.feedback.request.FeedbackSearchCriteria;
import com.talentradar.assessment_service.dto.feedback.response.FeedbackDto;
import com.talentradar.assessment_service.service.CommentService;
import com.talentradar.assessment_service.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final CommentService commentService;

    // ============= FEEDBACK ENDPOINTS =============
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<FeedbackDto>>> getAllFeedback() {
        List<FeedbackDto> feedback = feedbackService.getAllFeedback();
        return ResponseEntity.ok(ApiResponse.success(feedback, "Feedbacks and retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FeedbackDto>> getFeedbackById(@PathVariable UUID id) {
        FeedbackDto feedback = feedbackService.getFeedbackById(id);
        return ResponseEntity.ok(ApiResponse.success(feedback, "Feedback retrieved successfully"));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<ApiResponse<FeedbackDto>> getFeedbackWithDetails(@PathVariable UUID id) {
        FeedbackDto feedback = feedbackService.getFeedbackWithDetails(id);
        return ResponseEntity.ok(ApiResponse.success(feedback, "Feedback with details retrieved successfully"));
    }

    @GetMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<FeedbackDto>> getCompleteFeedback(@PathVariable UUID id) {
        FeedbackDto feedback = feedbackService.getCompleteFeedback(id);
        return ResponseEntity.ok(ApiResponse.success(feedback, "Complete feedback retrieved successfully"));
    }

    @GetMapping("/manager/{managerId}")
    public ResponseEntity<ApiResponse<List<FeedbackDto>>> getFeedbackByManagerId(@PathVariable UUID managerId) {
        List<FeedbackDto> feedback = feedbackService.getFeedbackByManagerId(managerId);
        return ResponseEntity.ok(ApiResponse.success(feedback, "Manager feedback retrieved successfully"));
    }

    @GetMapping("/developer/{developerId}")
    public ResponseEntity<ApiResponse<List<FeedbackDto>>> getFeedbackByDeveloperId(@PathVariable UUID developerId) {
        List<FeedbackDto> feedback = feedbackService.getFeedbackByDeveloperId(developerId);
        return ResponseEntity.ok(ApiResponse.success(feedback, "Developer feedback retrieved successfully"));
    }

    @GetMapping("/manager/{managerId}/developer/{developerId}")
    public ResponseEntity<ApiResponse<List<FeedbackDto>>> getFeedbackByManagerAndDeveloper(
            @PathVariable UUID managerId, @PathVariable UUID developerId) {
        List<FeedbackDto> feedback = feedbackService.getFeedbackByManagerAndDeveloper(managerId, developerId);
        return ResponseEntity.ok(ApiResponse.success(feedback, "Manager-developer feedback history retrieved successfully"));
    }

    @GetMapping("/manager/{managerId}/developer/{developerId}/latest")
    public ResponseEntity<ApiResponse<FeedbackDto>> getLatestFeedbackVersion(
            @PathVariable UUID managerId, @PathVariable UUID developerId) {
        FeedbackDto feedback = feedbackService.getLatestFeedbackVersion(managerId, developerId);
        return ResponseEntity.ok(ApiResponse.success(feedback, "Latest feedback version retrieved successfully"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FeedbackDto>> createCompleteFeedback(
            @Valid @RequestBody CreateCompleteFeedbackDto createDto) {
        FeedbackDto createdFeedback = feedbackService.createCompleteFeedback(createDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdFeedback, "Complete feedback created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FeedbackDto>> updateCompleteFeedback(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCompleteFeedbackDto updateDto) {
        FeedbackDto updatedFeedback = feedbackService.updateCompleteFeedback(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success(updatedFeedback, "Complete feedback updated successfully"));
    }

    @PostMapping("/manager/{managerId}/developer/{developerId}/new-version")
    public ResponseEntity<ApiResponse<FeedbackDto>> createNewFeedbackVersion(
            @PathVariable UUID managerId, @PathVariable UUID developerId) {
        FeedbackDto feedback = feedbackService.createNewFeedbackVersion(managerId, developerId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(feedback, "New feedback version created successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFeedback(@PathVariable UUID id) {
        feedbackService.deleteFeedback(id);
        return ResponseEntity.ok(ApiResponse.success("Feedback deleted successfully"));
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<ApiResponse<Boolean>> checkFeedbackExists(@PathVariable UUID id) {
        boolean exists = feedbackService.feedbackExists(id);
        String message = exists ? "Feedback exists" : "Feedback does not exist";
        return ResponseEntity.ok(ApiResponse.success(exists, message));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<FeedbackDto>>> getFeedbackByIds(
            @RequestBody List<UUID> ids) {
        List<FeedbackDto> feedback = feedbackService.getFeedbackByIds(ids);
        return ResponseEntity.ok(ApiResponse.success(feedback, "Feedback retrieved successfully"));
    }

    // ============= COMMENT TEMPLATE ENDPOINTS =============

    @GetMapping("/comments/templates")
    public ResponseEntity<ApiResponse<List<CommentDto>>> getAllCommentTemplates() {
        List<CommentDto> comments = commentService.getAllComments();
        return ResponseEntity.ok(ApiResponse.success(comments, "Comment templates retrieved successfully"));
    }

    @GetMapping("/comments/templates/{id}")
    public ResponseEntity<ApiResponse<CommentDto>> getCommentTemplateById(@PathVariable UUID id) {
        CommentDto comment = commentService.getCommentById(id);
        return ResponseEntity.ok(ApiResponse.success(comment, "Comment template retrieved successfully"));
    }

    @PostMapping("/comments/templates")
    public ResponseEntity<ApiResponse<CommentDto>> createCommentTemplate(
            @Valid @RequestBody CreateCommentDto createDto) {
        CommentDto createdComment = commentService.createComment(createDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdComment, "Comment template created successfully"));
    }

    @PutMapping("/comments/templates/{id}")
    public ResponseEntity<ApiResponse<CommentDto>> updateCommentTemplate(
            @PathVariable UUID id,
            @Valid @RequestBody CreateCommentDto updateDto) {
        CommentDto updatedComment = commentService.updateComment(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success(updatedComment, "Comment template updated successfully"));
    }

    @DeleteMapping("/comments/templates/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCommentTemplate(@PathVariable UUID id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success("Comment template deleted successfully"));
    }
    @GetMapping("/search")
    @PreAuthorize("hasRole('MANAGER') or hasRole('DEVELOPER')")
    public ResponseEntity<ApiResponse<PaginatedResponseDTO<FeedbackDto>>> searchFeedbacks(
            @ParameterObject FeedbackSearchCriteria criteria,
            @ParameterObject Pageable pageable) {
        PaginatedResponseDTO<FeedbackDto> feedbacks = feedbackService.searchFeedbacks(criteria, pageable);
        return ResponseEntity.ok(ApiResponse.success(feedbacks, "Feedbacks retrieved successfully"));
    }
}