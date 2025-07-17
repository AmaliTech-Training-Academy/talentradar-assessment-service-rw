package com.talentradar.assessment_service.controller;

import com.talentradar.assessment_service.dto.gradingCriteria.*;
import com.talentradar.assessment_service.dto.dimensionDefinition.*;
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
    public ResponseEntity<List<DimensionDefinitionDto>> getAllDimensions() {
        List<DimensionDefinitionDto> dimensions = dimensionDefinitionService.getAllDimensions();
        return ResponseEntity.ok(dimensions);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DimensionDefinitionDto> getDimensionById(@PathVariable UUID id) {
        DimensionDefinitionDto dimension = dimensionDefinitionService.getDimensionById(id);
        return ResponseEntity.ok(dimension);
    }

    @PostMapping
    public ResponseEntity<DimensionDefinitionDto> createDimension(
            @Valid @RequestBody CreateDimensionDefinitionDto createDto) {
        DimensionDefinitionDto createdDimension = dimensionDefinitionService.createDimension(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdDimension);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DimensionDefinitionDto> updateDimension(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDimensionDefinitionDto updateDto) {
        DimensionDefinitionDto updatedDimension = dimensionDefinitionService.updateDimension(id, updateDto);
        return ResponseEntity.ok(updatedDimension);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDimension(@PathVariable UUID id) {
        dimensionDefinitionService.deleteDimension(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/exists/{id}")
    public ResponseEntity<Boolean> checkDimensionExists(@PathVariable UUID id) {
        boolean exists = dimensionDefinitionService.dimensionExists(id);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/batch")
    public ResponseEntity<List<DimensionDefinitionDto>> getDimensionsByIds(
            @RequestBody List<UUID> ids) {
        List<DimensionDefinitionDto> dimensions = dimensionDefinitionService.getDimensionsByIds(ids);
        return ResponseEntity.ok(dimensions);
    }


    @GetMapping("/grading-criteria")
    public ResponseEntity<List<GradingCriteriaDto>> getAllGradingCriteria() {
        List<GradingCriteriaDto> gradingCriteria = gradingCriteriaService.getAllGradingCriteria();
        return ResponseEntity.ok(gradingCriteria);
    }

    @GetMapping("/grading-criteria/{id}")
    public ResponseEntity<GradingCriteriaDto> getGradingCriteriaById(@PathVariable UUID id) {
        GradingCriteriaDto gradingCriteria = gradingCriteriaService.getGradingCriteriaById(id);
        return ResponseEntity.ok(gradingCriteria);
    }

    @PostMapping("/grading-criteria")
    public ResponseEntity<GradingCriteriaDto> createGradingCriteria(
            @Valid @RequestBody CreateGradingCriteriaDto createDto) {
        GradingCriteriaDto createdGradingCriteria = gradingCriteriaService.createGradingCriteria(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdGradingCriteria);
    }

    @PutMapping("/grading-criteria/{id}")
    public ResponseEntity<GradingCriteriaDto> updateGradingCriteria(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateGradingCriteriaDto updateDto) {
        GradingCriteriaDto updatedGradingCriteria = gradingCriteriaService.updateGradingCriteria(id, updateDto);
        return ResponseEntity.ok(updatedGradingCriteria);
    }

    @DeleteMapping("/grading-criteria/{id}")
    public ResponseEntity<Void> deleteGradingCriteria(@PathVariable UUID id) {
        gradingCriteriaService.deleteGradingCriteria(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/grading-criteria/exists/{id}")
    public ResponseEntity<Boolean> checkGradingCriteriaExists(@PathVariable UUID id) {
        boolean exists = gradingCriteriaService.gradingCriteriaExists(id);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/grading-criteria/batch")
    public ResponseEntity<List<GradingCriteriaDto>> getGradingCriteriaByIds(
            @RequestBody List<UUID> ids) {
        List<GradingCriteriaDto> gradingCriteria = gradingCriteriaService.getGradingCriteriaByIds(ids);
        return ResponseEntity.ok(gradingCriteria);
    }
}