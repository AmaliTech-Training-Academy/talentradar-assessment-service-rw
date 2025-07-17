package com.talentradar.assessment_service.controller;

import com.talentradar.assessment_service.dto.dimensionDefinition.*;
import com.talentradar.assessment_service.dto.gradingCriteria.*;
import com.talentradar.assessment_service.dto.api.ApiResponse;
import com.talentradar.assessment_service.service.DimensionDefinitionService;
import com.talentradar.assessment_service.service.GradingCriteriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dimensions")
@RequiredArgsConstructor
public class DimensionDefinitionController {

    private final DimensionDefinitionService dimensionDefinitionService;
    private final GradingCriteriaService gradingCriteriaService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DimensionDefinitionDto>>> getAllDimensions() {
        List<DimensionDefinitionDto> dimensions = dimensionDefinitionService.getAllDimensions();
        return ResponseEntity.ok(ApiResponse.success(dimensions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DimensionDefinitionDto>> getDimensionById(@PathVariable UUID id) {
        DimensionDefinitionDto dimension = dimensionDefinitionService.getDimensionById(id);
        return ResponseEntity.ok(ApiResponse.success(dimension));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<DimensionDefinitionDto>> createDimension(
            @Valid @RequestBody CreateDimensionDefinitionDto createDto) {
        DimensionDefinitionDto createdDimension = dimensionDefinitionService.createDimension(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdDimension));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DimensionDefinitionDto>> updateDimension(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDimensionDefinitionDto updateDto) {
        DimensionDefinitionDto updatedDimension = dimensionDefinitionService.updateDimension(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success(updatedDimension));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDimension(@PathVariable UUID id) {
        dimensionDefinitionService.deleteDimension(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<ApiResponse<Boolean>> checkDimensionExists(@PathVariable UUID id) {
        boolean exists = dimensionDefinitionService.dimensionExists(id);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @PostMapping("/batch")
    public ResponseEntity<ApiResponse<List<DimensionDefinitionDto>>> getDimensionsByIds(
            @RequestBody List<UUID> ids) {
        List<DimensionDefinitionDto> dimensions = dimensionDefinitionService.getDimensionsByIds(ids);
        return ResponseEntity.ok(ApiResponse.success(dimensions));
    }

    // ============= GRADING CRITERIA ENDPOINTS =============

    @GetMapping("/grading-criteria")
    public ResponseEntity<ApiResponse<List<GradingCriteriaDto>>> getAllGradingCriteria() {
        List<GradingCriteriaDto> gradingCriteria = gradingCriteriaService.getAllGradingCriteria();
        return ResponseEntity.ok(ApiResponse.success(gradingCriteria));
    }

    @GetMapping("/grading-criteria/{id}")
    public ResponseEntity<ApiResponse<GradingCriteriaDto>> getGradingCriteriaById(@PathVariable UUID id) {
        GradingCriteriaDto gradingCriteria = gradingCriteriaService.getGradingCriteriaById(id);
        return ResponseEntity.ok(ApiResponse.success(gradingCriteria));
    }

    @PostMapping("/grading-criteria")
    public ResponseEntity<ApiResponse<GradingCriteriaDto>> createGradingCriteria(
            @Valid @RequestBody CreateGradingCriteriaDto createDto) {
        GradingCriteriaDto createdGradingCriteria = gradingCriteriaService.createGradingCriteria(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(createdGradingCriteria));
    }

    @PutMapping("/grading-criteria/{id}")
    public ResponseEntity<ApiResponse<GradingCriteriaDto>> updateGradingCriteria(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGradingCriteriaDto updateDto) {
        GradingCriteriaDto updatedGradingCriteria = gradingCriteriaService.updateGradingCriteria(id, updateDto);
        return ResponseEntity.ok(ApiResponse.success(updatedGradingCriteria));
    }

    @DeleteMapping("/grading-criteria/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGradingCriteria(@PathVariable UUID id) {
        gradingCriteriaService.deleteGradingCriteria(id);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/grading-criteria/exists/{id}")
    public ResponseEntity<ApiResponse<Boolean>> checkGradingCriteriaExists(@PathVariable UUID id) {
        boolean exists = gradingCriteriaService.gradingCriteriaExists(id);
        return ResponseEntity.ok(ApiResponse.success(exists));
    }

    @PostMapping("/grading-criteria/batch")
    public ResponseEntity<ApiResponse<List<GradingCriteriaDto>>> getGradingCriteriaByIds(
            @RequestBody List<UUID> ids) {
        List<GradingCriteriaDto> gradingCriteria = gradingCriteriaService.getGradingCriteriaByIds(ids);
        return ResponseEntity.ok(ApiResponse.success(gradingCriteria));
    }
}