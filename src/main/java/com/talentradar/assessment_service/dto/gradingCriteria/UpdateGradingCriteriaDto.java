package com.talentradar.assessment_service.dto.gradingCriteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateGradingCriteriaDto {
    private String criteriaName;
}