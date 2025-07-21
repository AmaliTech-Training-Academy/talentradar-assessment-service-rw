package com.talentradar.assessment_service.dto.userSnapshot.request;

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
public class AssignDeveloperToManagerDto {
    
    @NotNull(message = "Manager ID is required")
    private UUID managerId;
}