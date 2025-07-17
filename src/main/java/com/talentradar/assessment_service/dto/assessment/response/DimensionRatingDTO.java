package com.talentradar.assessment_service.dto.assessment.response;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
public class DimensionRatingDTO {
    @NotNull
    private UUID dimensionDefinitionId;

    @Min(1)
    @Max(5)
    private int rating;
}

