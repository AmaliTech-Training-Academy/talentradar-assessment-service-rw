package com.talentradar.assessment_service.dto.feedback.request;

import com.talentradar.assessment_service.dto.dimensionDefinition.request.CreateFeedbackDimensionRequestDto;
import com.talentradar.assessment_service.dto.feedbackComment.request.CreateFeedbackCommentRequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCompleteFeedbackDto {
    
    @NotNull(message = "Manager ID is required")
    private UUID managerId;
    
    @NotNull(message = "Developer ID is required")
    private UUID developerId;
    
    @Min(value = 1, message = "Feedback version must be at least 1")
    private int feedbackVersion = 1;
    
    @NotEmpty(message = "Dimensions are required")
    @Valid
    private List<CreateFeedbackDimensionRequestDto> dimensions;
    
    @Valid
    private List<CreateFeedbackCommentRequestDto> feedbackComments;
}