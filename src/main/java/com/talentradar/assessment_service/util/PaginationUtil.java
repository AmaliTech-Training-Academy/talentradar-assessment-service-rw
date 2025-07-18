package com.talentradar.assessment_service.util;

import com.talentradar.assessment_service.dto.assessment.request.PaginationMetadata;
import com.talentradar.assessment_service.dto.assessment.response.PaginatedResponseDTO;
import org.springframework.data.domain.Page;

public class PaginationUtil {

    public static <T> PaginatedResponseDTO<T> toPaginatedResponse(Page<T> pageData) {
        return PaginatedResponseDTO.<T>builder()
                .content(pageData.getContent())
                .pagination(PaginationMetadata.builder()
                        .page(pageData.getNumber())
                        .size(pageData.getSize())
                        .totalElements(pageData.getTotalElements())
                        .totalPages(pageData.getTotalPages())
                        .hasNext(pageData.hasNext())
                        .hasPrevious(pageData.hasPrevious())
                        .build())
                .build();
    }
}

