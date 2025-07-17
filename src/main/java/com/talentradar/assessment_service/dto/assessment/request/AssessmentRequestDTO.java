package com.talentradar.assessment_service.dto.assessment.request;

import com.talentradar.assessment_service.model.SubmissionStatus;
import jakarta.validation.constraints.NotBlank;
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
public class AssessmentRequestDTO {
    @NotNull
    private UUID userId;

    @NotBlank
    private String reflection;

    @NotNull
    private SubmissionStatus status;

    @NotEmpty
    private List<DimensionRatingDTO> dimensions;
}
