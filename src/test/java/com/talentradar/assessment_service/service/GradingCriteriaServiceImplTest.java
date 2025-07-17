package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.gradingCriteria.request.CreateGradingCriteriaDto;
import com.talentradar.assessment_service.dto.gradingCriteria.response.GradingCriteriaDto;
import com.talentradar.assessment_service.dto.gradingCriteria.request.UpdateGradingCriteriaDto;
import com.talentradar.assessment_service.exception.GradingCriteriaNotFoundException;
import com.talentradar.assessment_service.model.GradingCriteria;
import com.talentradar.assessment_service.repository.GradingCriteriaRepository;
import com.talentradar.assessment_service.service.impl.GradingCriteriaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GradingCriteriaService Tests")
class GradingCriteriaServiceImplTest {

    @Mock
    private GradingCriteriaRepository gradingCriteriaRepository;

    @InjectMocks
    private GradingCriteriaServiceImpl gradingCriteriaService;

    private GradingCriteria sampleGradingCriteria;
    private CreateGradingCriteriaDto createDto;
    private UpdateGradingCriteriaDto updateDto;
    private UUID gradingCriteriaId;

    @BeforeEach
    void setUp() {
        gradingCriteriaId = UUID.randomUUID();
        
        sampleGradingCriteria = GradingCriteria.builder()
                .id(gradingCriteriaId)
                .criteriaName("Code Quality")
                .dimensionDefinitions(new HashSet<>())
                .build();

        createDto = CreateGradingCriteriaDto.builder()
                .criteriaName("Problem Solving")
                .build();

        updateDto = UpdateGradingCriteriaDto.builder()
                .criteriaName("Updated Code Quality")
                .build();
    }

    @Test
    @DisplayName("Should return all grading criteria successfully")
    void getAllGradingCriteria_ShouldReturnAllGradingCriteria() {
        // Given
        List<GradingCriteria> gradingCriteriaList = Arrays.asList(sampleGradingCriteria);
        when(gradingCriteriaRepository.findAll()).thenReturn(gradingCriteriaList);

        // When
        List<GradingCriteriaDto> result = gradingCriteriaService.getAllGradingCriteria();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(gradingCriteriaId);
        assertThat(result.get(0).getCriteriaName()).isEqualTo("Code Quality");
        
        verify(gradingCriteriaRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no grading criteria exist")
    void getAllGradingCriteria_ShouldReturnEmptyList_WhenNoGradingCriteriaExist() {
        // Given
        when(gradingCriteriaRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<GradingCriteriaDto> result = gradingCriteriaService.getAllGradingCriteria();

        // Then
        assertThat(result).isEmpty();
        verify(gradingCriteriaRepository).findAll();
    }

    @Test
    @DisplayName("Should return grading criteria by ID successfully")
    void getGradingCriteriaById_ShouldReturnGradingCriteria_WhenIdExists() {
        // Given
        when(gradingCriteriaRepository.findById(gradingCriteriaId)).thenReturn(Optional.of(sampleGradingCriteria));

        // When
        GradingCriteriaDto result = gradingCriteriaService.getGradingCriteriaById(gradingCriteriaId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradingCriteriaId);
        assertThat(result.getCriteriaName()).isEqualTo("Code Quality");
        
        verify(gradingCriteriaRepository).findById(gradingCriteriaId);
    }

    @Test
    @DisplayName("Should throw exception when grading criteria not found by ID")
    void getGradingCriteriaById_ShouldThrowException_WhenIdNotExists() {
        // Given
        when(gradingCriteriaRepository.findById(gradingCriteriaId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gradingCriteriaService.getGradingCriteriaById(gradingCriteriaId))
                .isInstanceOf(GradingCriteriaNotFoundException.class)
                .hasMessage("Grading criteria not found with id: " + gradingCriteriaId);
        
        verify(gradingCriteriaRepository).findById(gradingCriteriaId);
    }

    @Test
    @DisplayName("Should create grading criteria successfully")
    void createGradingCriteria_ShouldCreateGradingCriteria_WhenValidDto() {
        // Given
        GradingCriteria newGradingCriteria = GradingCriteria.builder()
                .id(UUID.randomUUID())
                .criteriaName(createDto.getCriteriaName())
                .dimensionDefinitions(new HashSet<>())
                .build();
        
        when(gradingCriteriaRepository.save(any(GradingCriteria.class))).thenReturn(newGradingCriteria);

        // When
        GradingCriteriaDto result = gradingCriteriaService.createGradingCriteria(createDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCriteriaName()).isEqualTo("Problem Solving");
        
        verify(gradingCriteriaRepository).save(any(GradingCriteria.class));
    }

    @Test
    @DisplayName("Should update grading criteria successfully when grading criteria exists")
    void updateGradingCriteria_ShouldUpdateGradingCriteria_WhenIdExists() {
        // Given
        when(gradingCriteriaRepository.findById(gradingCriteriaId)).thenReturn(Optional.of(sampleGradingCriteria));
        when(gradingCriteriaRepository.save(any(GradingCriteria.class))).thenReturn(sampleGradingCriteria);

        // When
        GradingCriteriaDto result = gradingCriteriaService.updateGradingCriteria(gradingCriteriaId, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(gradingCriteriaRepository).findById(gradingCriteriaId);
        verify(gradingCriteriaRepository).save(sampleGradingCriteria);
        
        // Verify the grading criteria was updated
        assertThat(sampleGradingCriteria.getCriteriaName()).isEqualTo("Updated Code Quality");
    }

    @Test
    @DisplayName("Should not update when criteria name is null")
    void updateGradingCriteria_ShouldNotUpdate_WhenCriteriaNameIsNull() {
        // Given
        UpdateGradingCriteriaDto nullUpdateDto = UpdateGradingCriteriaDto.builder()
                .criteriaName(null)
                .build();
        
        String originalName = sampleGradingCriteria.getCriteriaName();
        when(gradingCriteriaRepository.findById(gradingCriteriaId)).thenReturn(Optional.of(sampleGradingCriteria));
        when(gradingCriteriaRepository.save(any(GradingCriteria.class))).thenReturn(sampleGradingCriteria);

        // When
        gradingCriteriaService.updateGradingCriteria(gradingCriteriaId, nullUpdateDto);

        // Then
        assertThat(sampleGradingCriteria.getCriteriaName()).isEqualTo(originalName); // unchanged
        
        verify(gradingCriteriaRepository).findById(gradingCriteriaId);
        verify(gradingCriteriaRepository).save(sampleGradingCriteria);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent grading criteria")
    void updateGradingCriteria_ShouldThrowException_WhenIdNotExists() {
        // Given
        when(gradingCriteriaRepository.findById(gradingCriteriaId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gradingCriteriaService.updateGradingCriteria(gradingCriteriaId, updateDto))
                .isInstanceOf(GradingCriteriaNotFoundException.class)
                .hasMessage("Grading criteria not found with id: " + gradingCriteriaId);
        
        verify(gradingCriteriaRepository).findById(gradingCriteriaId);
        verify(gradingCriteriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete grading criteria successfully when grading criteria exists")
    void deleteGradingCriteria_ShouldDeleteGradingCriteria_WhenIdExists() {
        // Given
        when(gradingCriteriaRepository.findById(gradingCriteriaId)).thenReturn(Optional.of(sampleGradingCriteria));

        // When
        gradingCriteriaService.deleteGradingCriteria(gradingCriteriaId);

        // Then
        verify(gradingCriteriaRepository).findById(gradingCriteriaId);
        verify(gradingCriteriaRepository).delete(sampleGradingCriteria);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent grading criteria")
    void deleteGradingCriteria_ShouldThrowException_WhenIdNotExists() {
        // Given
        when(gradingCriteriaRepository.findById(gradingCriteriaId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> gradingCriteriaService.deleteGradingCriteria(gradingCriteriaId))
                .isInstanceOf(GradingCriteriaNotFoundException.class)
                .hasMessage("Grading criteria not found with id: " + gradingCriteriaId);
        
        verify(gradingCriteriaRepository).findById(gradingCriteriaId);
        verify(gradingCriteriaRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should return true when grading criteria exists")
    void gradingCriteriaExists_ShouldReturnTrue_WhenIdExists() {
        // Given
        when(gradingCriteriaRepository.existsById(gradingCriteriaId)).thenReturn(true);

        // When
        boolean result = gradingCriteriaService.gradingCriteriaExists(gradingCriteriaId);

        // Then
        assertThat(result).isTrue();
        verify(gradingCriteriaRepository).existsById(gradingCriteriaId);
    }
}