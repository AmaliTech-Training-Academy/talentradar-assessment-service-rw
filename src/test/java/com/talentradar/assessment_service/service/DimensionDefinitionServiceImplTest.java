package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.dimensionDefinition.request.CreateDimensionDefinitionDto;
import com.talentradar.assessment_service.dto.dimensionDefinition.response.DimensionDefinitionDto;
import com.talentradar.assessment_service.dto.dimensionDefinition.request.UpdateDimensionDefinitionDto;
import com.talentradar.assessment_service.exception.DimensionDefinitionNotFoundException;
import com.talentradar.assessment_service.model.DimensionDefinition;
import com.talentradar.assessment_service.repository.DimensionDefinitionRepository;
import com.talentradar.assessment_service.service.impl.DimensionDefinitionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DimensionDefinitionService Tests")
class DimensionDefinitionServiceImplTest {

    @Mock
    private DimensionDefinitionRepository dimensionDefinitionRepository;

    @InjectMocks
    private DimensionDefinitionServiceImpl dimensionDefinitionService;

    private DimensionDefinition sampleDimension;
    private CreateDimensionDefinitionDto createDto;
    private UpdateDimensionDefinitionDto updateDto;
    private UUID dimensionId;

    @BeforeEach
    void setUp() {
        dimensionId = UUID.randomUUID();

        sampleDimension = DimensionDefinition.builder()
                .id(dimensionId)
                .dimensionName("Technical Skills")
                .description("Assessment of technical capabilities")
                .weight(new BigDecimal("25.50"))
                .build();

        createDto = CreateDimensionDefinitionDto.builder()
                .dimensionName("Communication Skills")
                .description("Assessment of communication abilities")
                .weight(new BigDecimal("20.00"))
                .build();

        updateDto = UpdateDimensionDefinitionDto.builder()
                .dimensionName("Updated Technical Skills")
                .description("Updated description")
                .weight(new BigDecimal("30.00"))
                .build();
    }

    @Test
    @DisplayName("Should return all dimensions successfully")
    void getAllDimensions_ShouldReturnAllDimensions() {
        // Given
        List<DimensionDefinition> dimensions = Arrays.asList(sampleDimension);
        when(dimensionDefinitionRepository.findAll()).thenReturn(dimensions);

        // When
        List<DimensionDefinitionDto> result = dimensionDefinitionService.getAllDimensions();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(dimensionId);
        assertThat(result.get(0).getDimensionName()).isEqualTo("Technical Skills");
        assertThat(result.get(0).getDescription()).isEqualTo("Assessment of technical capabilities");
        assertThat(result.get(0).getWeight()).isEqualTo(new BigDecimal("25.50"));

        verify(dimensionDefinitionRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no dimensions exist")
    void getAllDimensions_ShouldReturnEmptyList_WhenNoDimensionsExist() {
        // Given
        when(dimensionDefinitionRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<DimensionDefinitionDto> result = dimensionDefinitionService.getAllDimensions();

        // Then
        assertThat(result).isEmpty();
        verify(dimensionDefinitionRepository).findAll();
    }

    @Test
    @DisplayName("Should return dimension by ID successfully")
    void getDimensionById_ShouldReturnDimension_WhenIdExists() {
        // Given
        when(dimensionDefinitionRepository.findById(dimensionId)).thenReturn(Optional.of(sampleDimension));

        // When
        DimensionDefinitionDto result = dimensionDefinitionService.getDimensionById(dimensionId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(dimensionId);
        assertThat(result.getDimensionName()).isEqualTo("Technical Skills");
        assertThat(result.getDescription()).isEqualTo("Assessment of technical capabilities");
        assertThat(result.getWeight()).isEqualTo(new BigDecimal("25.50"));

        verify(dimensionDefinitionRepository).findById(dimensionId);
    }

    @Test
    @DisplayName("Should throw exception when dimension not found by ID")
    void getDimensionById_ShouldThrowException_WhenIdNotExists() {
        // Given
        when(dimensionDefinitionRepository.findById(dimensionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dimensionDefinitionService.getDimensionById(dimensionId))
                .isInstanceOf(DimensionDefinitionNotFoundException.class)
                .hasMessage("Dimension not found with id: " + dimensionId);

        verify(dimensionDefinitionRepository).findById(dimensionId);
    }

    @Test
    @DisplayName("Should create dimension successfully")
    void createDimension_ShouldCreateDimension_WhenValidDto() {
        // Given
        DimensionDefinition newDimension = DimensionDefinition.builder()
                .id(UUID.randomUUID())
                .dimensionName(createDto.getDimensionName())
                .description(createDto.getDescription())
                .weight(createDto.getWeight())
                .build();

        when(dimensionDefinitionRepository.save(any(DimensionDefinition.class))).thenReturn(newDimension);

        // When
        DimensionDefinitionDto result = dimensionDefinitionService.createDimension(createDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDimensionName()).isEqualTo("Communication Skills");
        assertThat(result.getDescription()).isEqualTo("Assessment of communication abilities");
        assertThat(result.getWeight()).isEqualTo(new BigDecimal("20.00"));

        verify(dimensionDefinitionRepository).save(any(DimensionDefinition.class));
    }

    @Test
    @DisplayName("Should update dimension successfully when dimension exists")
    void updateDimension_ShouldUpdateDimension_WhenIdExists() {
        // Given
        when(dimensionDefinitionRepository.findById(dimensionId)).thenReturn(Optional.of(sampleDimension));
        when(dimensionDefinitionRepository.save(any(DimensionDefinition.class))).thenReturn(sampleDimension);

        // When
        DimensionDefinitionDto result = dimensionDefinitionService.updateDimension(dimensionId, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(dimensionDefinitionRepository).findById(dimensionId);
        verify(dimensionDefinitionRepository).save(sampleDimension);

        // Verify the dimension was updated
        assertThat(sampleDimension.getDimensionName()).isEqualTo("Updated Technical Skills");
        assertThat(sampleDimension.getDescription()).isEqualTo("Updated description");
        assertThat(sampleDimension.getWeight()).isEqualTo(new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("Should update only provided fields")
    void updateDimension_ShouldUpdateOnlyProvidedFields() {
        // Given
        UpdateDimensionDefinitionDto partialUpdateDto = UpdateDimensionDefinitionDto.builder()
                .dimensionName("Only Name Updated")
                .build();

        when(dimensionDefinitionRepository.findById(dimensionId)).thenReturn(Optional.of(sampleDimension));
        when(dimensionDefinitionRepository.save(any(DimensionDefinition.class))).thenReturn(sampleDimension);

        // When
        dimensionDefinitionService.updateDimension(dimensionId, partialUpdateDto);

        // Then
        assertThat(sampleDimension.getDimensionName()).isEqualTo("Only Name Updated");
        assertThat(sampleDimension.getDescription()).isEqualTo("Assessment of technical capabilities"); // unchanged
        assertThat(sampleDimension.getWeight()).isEqualTo(new BigDecimal("25.50")); // unchanged

        verify(dimensionDefinitionRepository).findById(dimensionId);
        verify(dimensionDefinitionRepository).save(sampleDimension);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent dimension")
    void updateDimension_ShouldThrowException_WhenIdNotExists() {
        // Given
        when(dimensionDefinitionRepository.findById(dimensionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dimensionDefinitionService.updateDimension(dimensionId, updateDto))
                .isInstanceOf(DimensionDefinitionNotFoundException.class)
                .hasMessage("Dimension not found with id: " + dimensionId);

        verify(dimensionDefinitionRepository).findById(dimensionId);
        verify(dimensionDefinitionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete dimension successfully when dimension exists")
    void deleteDimension_ShouldDeleteDimension_WhenIdExists() {
        // Given
        when(dimensionDefinitionRepository.findById(dimensionId)).thenReturn(Optional.of(sampleDimension));

        // When
        dimensionDefinitionService.deleteDimension(dimensionId);

        // Then
        verify(dimensionDefinitionRepository).findById(dimensionId);
        verify(dimensionDefinitionRepository).delete(sampleDimension);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent dimension")
    void deleteDimension_ShouldThrowException_WhenIdNotExists() {
        // Given
        when(dimensionDefinitionRepository.findById(dimensionId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> dimensionDefinitionService.deleteDimension(dimensionId))
                .isInstanceOf(DimensionDefinitionNotFoundException.class)
                .hasMessage("Dimension not found with id: " + dimensionId);

        verify(dimensionDefinitionRepository).findById(dimensionId);
        verify(dimensionDefinitionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should return true when dimension exists")
    void dimensionExists_ShouldReturnTrue_WhenIdExists() {
        // Given
        when(dimensionDefinitionRepository.existsById(dimensionId)).thenReturn(true);

        // When
        boolean result = dimensionDefinitionService.dimensionExists(dimensionId);

        // Then
        assertThat(result).isTrue();
        verify(dimensionDefinitionRepository).existsById(dimensionId);
    }

}