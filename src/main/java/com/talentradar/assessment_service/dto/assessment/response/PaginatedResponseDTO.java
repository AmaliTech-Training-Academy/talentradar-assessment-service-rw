package com.talentradar.assessment_service.dto.assessment.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.talentradar.assessment_service.dto.assessment.request.PaginationMetadata;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaginatedResponseDTO<T> {
    private List<T> content;
    private PaginationMetadata pagination;
}

