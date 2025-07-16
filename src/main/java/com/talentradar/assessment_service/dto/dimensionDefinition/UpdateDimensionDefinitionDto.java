package com.talentradar.assessment_service.dto.dimensionDefinition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDimensionDefinitionDto {
    private String dimensionName;
    private String description;

    @DecimalMin(value = "0.0", message = "Weight must be positive")
    @DecimalMax(value = "100.0", message = "Weight cannot exceed 100")
    private BigDecimal weight;
}