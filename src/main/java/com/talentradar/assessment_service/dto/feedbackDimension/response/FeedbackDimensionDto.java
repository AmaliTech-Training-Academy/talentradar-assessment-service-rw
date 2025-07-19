package com.talentradar.assessment_service.dto.feedbackDimension.response;

import com.talentradar.assessment_service.dto.dimensionDefinition.response.DimensionDefinitionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackDimensionDto {
    private UUID id;
    private UUID feedbackId;
    private DimensionDefinitionDto dimensionDefinition;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}