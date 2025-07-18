package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.assessment.request.AssessmentRequestDTO;
import com.talentradar.assessment_service.dto.assessment.response.AssessmentResponseDTO;
import com.talentradar.assessment_service.dto.assessment.response.PaginatedResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AssessmentService {
    AssessmentResponseDTO createAssessment(AssessmentRequestDTO requestDto, UUID userId);
    PaginatedResponseDTO<AssessmentResponseDTO> getAssessmentsByUser(UUID userId, Pageable pageable);


}
