package com.talentradar.assessment_service.dto.assessment.response;

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
public class DimensionResponseDTO {
    private UUID id;
    private UUID assessmentId;
    private UUID dimensionDefinitionId;
    private int rating;
    private LocalDateTime createdAt;
}
