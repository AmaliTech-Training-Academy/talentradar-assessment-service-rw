package com.talentradar.assessment_service.dto.dimensionDefinition.response;

import com.talentradar.assessment_service.dto.gradingCriteria.response.GradingCriteriaDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DimensionDefinitionDto {
    private UUID id;
    private String dimensionName;
    private String description;
    private BigDecimal weight;
    private Set<GradingCriteriaDto> gradingCriteria;
}