package com.talentradar.assessment_service.dto.assessment.response;

import com.talentradar.assessment_service.model.SubmissionStatus;
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
public class AssessmentResponseDTO {
    private UUID id;
    private UUID userId;
    private String reflection;
    private SubmissionStatus status;
    private int average;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DimensionResponseDTO> dimensions;
}
