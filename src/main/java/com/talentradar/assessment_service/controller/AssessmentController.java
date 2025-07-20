package com.talentradar.assessment_service.controller;

import com.talentradar.assessment_service.dto.assessment.request.AssessmentRequestDTO;
import com.talentradar.assessment_service.dto.assessment.response.AssessmentResponseDTO;
import com.talentradar.assessment_service.dto.api.ApiResponse;
import com.talentradar.assessment_service.dto.assessment.response.PaginatedResponseDTO;
import com.talentradar.assessment_service.service.AssessmentService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assessments")
@RequiredArgsConstructor
public class AssessmentController {

    private final AssessmentService assessmentService;

    @PostMapping
    @PreAuthorize("hasRole('DEVELOPER')")
    public ResponseEntity<ApiResponse<AssessmentResponseDTO>> createAssessment(
            @Valid @RequestBody AssessmentRequestDTO requestDto,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdStr
    ) {
        UUID userId = UUID.fromString(userIdStr);
        AssessmentResponseDTO response = assessmentService.createAssessment(requestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Assessment submitted successfully"));
    }

    @GetMapping
    @PreAuthorize("hasRole('DEVELOPER') or hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<PaginatedResponseDTO<AssessmentResponseDTO>>> getUserAssessments(
            @RequestHeader("X-User-Id") UUID userId,
            @ParameterObject Pageable pageable) {

        PaginatedResponseDTO<AssessmentResponseDTO> pagedAssessments = assessmentService.getAllAssessmentsByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(pagedAssessments));
    }


}
