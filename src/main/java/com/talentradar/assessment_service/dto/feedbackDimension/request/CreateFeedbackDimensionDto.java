package com.talentradar.assessment_service.dto.feedbackDimension.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class CreateFeedbackDimensionDto {
    private UUID feedbackId;
    
    @NotNull(message = "Dimension definition ID is required")
    private UUID dimensionDefinitionId;
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;
    
    @NotBlank(message = "Comment is required")
    private String comment;
}