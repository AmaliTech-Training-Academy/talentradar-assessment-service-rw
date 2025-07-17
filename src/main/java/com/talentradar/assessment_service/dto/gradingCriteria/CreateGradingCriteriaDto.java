package com.talentradar.assessment_service.dto.gradingCriteria;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGradingCriteriaDto {
    @NotBlank(message = "Criteria name is required")
    private String criteriaName;
}