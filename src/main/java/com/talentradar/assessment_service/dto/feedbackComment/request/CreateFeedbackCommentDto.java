package com.talentradar.assessment_service.dto.feedbackComment.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateFeedbackCommentDto {
    
    @NotNull(message = "Feedback ID is required")
    private UUID feedbackId;
    
    @NotNull(message = "Comment ID is required")
    private UUID commentId;
    
    @NotBlank(message = "Feedback comment body is required")
    private String feedbackCommentBody;
}