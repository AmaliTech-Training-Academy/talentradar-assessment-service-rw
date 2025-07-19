package com.talentradar.assessment_service.dto.feedback.request;

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
public class FeedbackSearchCriteria {
    
    private UUID managerId;
    private UUID developerId;
    private Integer feedbackVersion;
    private LocalDateTime createdAfter;
    private LocalDateTime createdBefore;
}