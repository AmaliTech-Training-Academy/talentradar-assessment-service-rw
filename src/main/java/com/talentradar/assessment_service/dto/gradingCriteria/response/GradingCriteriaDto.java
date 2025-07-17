package com.talentradar.assessment_service.dto.gradingCriteria.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GradingCriteriaDto {
    private UUID id;
    private String criteriaName;
}