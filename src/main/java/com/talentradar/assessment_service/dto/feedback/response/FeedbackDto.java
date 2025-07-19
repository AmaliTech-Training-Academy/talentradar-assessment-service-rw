package com.talentradar.assessment_service.dto.feedback.response;

import com.talentradar.assessment_service.dto.feedbackComment.response.FeedbackCommentDto;
import com.talentradar.assessment_service.dto.feedbackDimension.response.FeedbackDimensionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackDto {
    private UUID id;
    private UUID managerId;
    private UUID developerId;
    private int feedbackVersion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<FeedbackDimensionDto> dimensions;
    private List<FeedbackCommentDto> feedbackComments;

}