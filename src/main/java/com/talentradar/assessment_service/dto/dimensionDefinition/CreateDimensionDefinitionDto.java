package com.talentradar.assessment_service.dto.dimensionDefinition;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateDimensionDefinitionDto {
    @NotBlank(message = "Dimension name is required")
    private String dimensionName;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.0", message = "Weight must be positive")
    @DecimalMax(value = "100.0", message = "Weight cannot exceed 100")
    private BigDecimal weight;

    private Set<UUID> gradingCriteriaIds;
}
