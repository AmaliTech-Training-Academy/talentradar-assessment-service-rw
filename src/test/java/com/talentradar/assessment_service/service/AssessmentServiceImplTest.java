package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.assessment.request.AssessmentRequestDTO;
import com.talentradar.assessment_service.dto.assessment.request.DimensionRatingDTO;
import com.talentradar.assessment_service.dto.assessment.response.AssessmentResponseDTO;
import com.talentradar.assessment_service.dto.assessment.response.PaginatedResponseDTO;
import com.talentradar.assessment_service.event.rabbit.producer.AssessmentEventProducer;
import com.talentradar.assessment_service.exception.BadRequestException;
import com.talentradar.assessment_service.exception.ResourceNotFoundException;
import com.talentradar.assessment_service.mapper.AssessmentMapper;
import com.talentradar.assessment_service.model.Assessment;
import com.talentradar.assessment_service.model.DimensionDefinition;
import com.talentradar.assessment_service.model.SubmissionStatus;
import com.talentradar.assessment_service.repository.AssessmentDimensionRepository;
import com.talentradar.assessment_service.repository.AssessmentRepository;
import com.talentradar.assessment_service.repository.DimensionDefinitionRepository;
import com.talentradar.assessment_service.service.impl.AssessmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceImplTest {

    @InjectMocks
    private AssessmentServiceImpl assessmentService;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private AssessmentDimensionRepository dimensionRepository;

    @Mock
    private DimensionDefinitionRepository dimensionDefinitionRepository;

    @Mock
    private AssessmentMapper assessmentMapper;

    @Mock(lenient = true)
    private AssessmentEventProducer assessmentEventProducer;

    private UUID userId;
    private UUID assessmentId;
    private UUID dimensionId1;
    private UUID dimensionId2;
    private AssessmentRequestDTO requestDto;
    private Assessment assessment;
    private AssessmentResponseDTO responseDto;
    private DimensionDefinition dimensionDefinition1;
    private DimensionDefinition dimensionDefinition2;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        assessmentId = UUID.randomUUID();
        dimensionId1 = UUID.randomUUID();
        dimensionId2 = UUID.randomUUID();

        requestDto = AssessmentRequestDTO.builder()
                .reflection("Reflection Text")
                .status(SubmissionStatus.SUBMITTED)
                .dimensions(List.of(
                        DimensionRatingDTO.builder()
                                .dimensionDefinitionId(dimensionId1)
                                .rating(4)
                                .build(),
                        DimensionRatingDTO.builder()
                                .dimensionDefinitionId(dimensionId2)
                                .rating(3)
                                .build()))
                .build();

        assessment = Assessment.builder()
                .id(assessmentId)
                .userId(userId)
                .reflection("Reflection Text")
                .submissionStatus(SubmissionStatus.SUBMITTED)
                .averageScore(4)
                .build();

        responseDto = AssessmentResponseDTO.builder()
                .id(assessment.getId())
                .userId(userId)
                .reflection("Reflection Text")
                .status(SubmissionStatus.SUBMITTED)
                .average(4)
                .build();

        // Technical Excellence - 25% weight
        dimensionDefinition1 = DimensionDefinition.builder()
                .id(dimensionId1)
                .dimensionName("Technical Excellence")
                .weight(new BigDecimal("0.25"))
                .build();

        // Communication - 20% weight
        dimensionDefinition2 = DimensionDefinition.builder()
                .id(dimensionId2)
                .dimensionName("Communication")
                .weight(new BigDecimal("0.20"))
                .build();
    }

    @Test
    void shouldCreateAssessmentSuccessfully() {
        // Arrange
        when(dimensionDefinitionRepository.findExistingIds(List.of(dimensionId1, dimensionId2)))
                .thenReturn(List.of(dimensionId1, dimensionId2));

        when(assessmentRepository.existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
                eq(userId),
                eq(SubmissionStatus.SUBMITTED),
                any(LocalDateTime.class)))
                .thenReturn(false);

        when(dimensionDefinitionRepository.findById(dimensionId1))
                .thenReturn(Optional.of(dimensionDefinition1));

        when(dimensionDefinitionRepository.findById(dimensionId2))
                .thenReturn(Optional.of(dimensionDefinition2));

        when(assessmentRepository.save(any(Assessment.class)))
                .thenReturn(assessment);

        when(dimensionRepository.saveAll(anyList()))
                .thenReturn(Collections.emptyList());

        when(assessmentMapper.toResponseDto(any(Assessment.class)))
                .thenReturn(responseDto);

        // Act
        AssessmentResponseDTO result = assessmentService.createAssessment(requestDto, userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(4, result.getAverage());

        // Verify dimension validation was called
        verify(dimensionDefinitionRepository).findExistingIds(List.of(dimensionId1, dimensionId2));

        // Verify re-submission validation was called
        verify(assessmentRepository).existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
                eq(userId), eq(SubmissionStatus.SUBMITTED), any(LocalDateTime.class));

        // Verify dimension definitions were fetched for weight calculation and dimension creation
        verify(dimensionDefinitionRepository, times(2)).findById(dimensionId1);
        verify(dimensionDefinitionRepository, times(2)).findById(dimensionId2);

        // Verify assessment was saved with correct average score
        verify(assessmentRepository).save(any(Assessment.class));
        verify(dimensionRepository).saveAll(anyList());
        verify(assessmentMapper).toResponseDto(any(Assessment.class));

        // Verify event was published
        verify(assessmentEventProducer).publishAssessmentSubmitted(any(Assessment.class));
    }

    @Test
    void shouldCalculateCorrectWeightedAverage() {
        // Arrange - Create a more complex scenario
        List<DimensionRatingDTO> complexDimensions = List.of(
                DimensionRatingDTO.builder().dimensionDefinitionId(dimensionId1).rating(5).build(),
                DimensionRatingDTO.builder().dimensionDefinitionId(dimensionId2).rating(3).build()
        );

        AssessmentRequestDTO complexRequest = AssessmentRequestDTO.builder()
                .reflection("Test reflection")
                .status(SubmissionStatus.SUBMITTED)
                .dimensions(complexDimensions)
                .build();

        // Set up dimension definitions with different weights
        dimensionDefinition1.setWeight(new BigDecimal("0.30")); // 30%
        dimensionDefinition2.setWeight(new BigDecimal("0.70")); // 70%

        when(dimensionDefinitionRepository.findExistingIds(List.of(dimensionId1, dimensionId2)))
                .thenReturn(List.of(dimensionId1, dimensionId2));

        when(assessmentRepository.existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
                eq(userId), eq(SubmissionStatus.SUBMITTED), any(LocalDateTime.class)))
                .thenReturn(false);

        when(dimensionDefinitionRepository.findById(dimensionId1))
                .thenReturn(Optional.of(dimensionDefinition1));

        when(dimensionDefinitionRepository.findById(dimensionId2))
                .thenReturn(Optional.of(dimensionDefinition2));

        when(assessmentRepository.save(any(Assessment.class)))
                .thenReturn(assessment);

        when(dimensionRepository.saveAll(anyList()))
                .thenReturn(Collections.emptyList());

        when(assessmentMapper.toResponseDto(any(Assessment.class)))
                .thenReturn(responseDto);

        // Act
        assessmentService.createAssessment(complexRequest, userId);

        // Assert - Expected calculation: (5*0.30 + 3*0.70) / 1.0 = (1.5 + 2.1) / 1.0 = 3.6 ≈ 4
        verify(assessmentRepository).save(argThat(savedAssessment ->
                savedAssessment.getAverageScore() == 4));
    }

    @Test
    void shouldThrowBadRequestForInvalidDimensionId() {
        // Arrange
        when(dimensionDefinitionRepository.findExistingIds(List.of(dimensionId1, dimensionId2)))
                .thenReturn(List.of(dimensionId1)); // Only one ID exists

        // Act & Assert
        assertThrows(BadRequestException.class, () ->
                assessmentService.createAssessment(requestDto, userId));

        verify(dimensionDefinitionRepository).findExistingIds(List.of(dimensionId1, dimensionId2));
        verifyNoInteractions(assessmentRepository, dimensionRepository, assessmentMapper);
    }

    @Test
    void shouldThrowResourceNotFoundIfDimensionMissing() {
        // Arrange
        when(dimensionDefinitionRepository.findExistingIds(List.of(dimensionId1, dimensionId2)))
                .thenReturn(List.of(dimensionId1, dimensionId2));

        when(assessmentRepository.existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
                eq(userId),
                eq(SubmissionStatus.SUBMITTED),
                any(LocalDateTime.class)))
                .thenReturn(false);

        // Mock first dimension to exist, second to not exist
        when(dimensionDefinitionRepository.findById(dimensionId1))
                .thenReturn(Optional.of(dimensionDefinition1));

        when(dimensionDefinitionRepository.findById(dimensionId2))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                assessmentService.createAssessment(requestDto, userId));

        assertEquals("DimensionDefinition with id " + dimensionId2 + " not found", exception.getMessage());

        verify(dimensionDefinitionRepository).findExistingIds(List.of(dimensionId1, dimensionId2));
        verify(assessmentRepository).existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
                eq(userId), eq(SubmissionStatus.SUBMITTED), any(LocalDateTime.class));
        verify(dimensionDefinitionRepository).findById(dimensionId1);
        verify(dimensionDefinitionRepository).findById(dimensionId2);
    }

    @Test
    void shouldThrowBadRequestIfUserSubmittedWithin30Days() {
        // Arrange
        when(dimensionDefinitionRepository.findExistingIds(List.of(dimensionId1, dimensionId2)))
                .thenReturn(List.of(dimensionId1, dimensionId2));

        when(assessmentRepository.existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
                eq(userId),
                eq(SubmissionStatus.SUBMITTED),
                any(LocalDateTime.class)))
                .thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> assessmentService.createAssessment(requestDto, userId));

        assertEquals("User has already submitted an assessment within the last 30 days.", exception.getMessage());

        verify(dimensionDefinitionRepository).findExistingIds(List.of(dimensionId1, dimensionId2));
        verify(assessmentRepository).existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
                eq(userId), eq(SubmissionStatus.SUBMITTED), any(LocalDateTime.class));
        verifyNoMoreInteractions(assessmentRepository, dimensionRepository, assessmentMapper);
    }

    @Test
    void shouldReturnPaginatedAssessmentsByUser() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Assessment> assessments = List.of(assessment);

        when(assessmentRepository.findAllByUserIdWithDimensions(userId, pageable))
                .thenReturn(new PageImpl<>(assessments));

        when(assessmentMapper.toResponseDto(any(Assessment.class)))
                .thenReturn(responseDto);

        // Act
        PaginatedResponseDTO<AssessmentResponseDTO> result = assessmentService.getAllAssessmentsByUser(userId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(userId, result.getContent().getFirst().getUserId());
        assertEquals(4, result.getContent().getFirst().getAverage());
        verify(assessmentRepository).findAllByUserIdWithDimensions(userId, pageable);
        verify(assessmentMapper).toResponseDto(any(Assessment.class));
    }

    @Test
    void shouldHandleZeroWeightGracefully() {
        // Arrange
        dimensionDefinition1.setWeight(BigDecimal.ZERO);
        dimensionDefinition2.setWeight(BigDecimal.ZERO);

        when(dimensionDefinitionRepository.findExistingIds(List.of(dimensionId1, dimensionId2)))
                .thenReturn(List.of(dimensionId1, dimensionId2));

        when(assessmentRepository.existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
                eq(userId), eq(SubmissionStatus.SUBMITTED), any(LocalDateTime.class)))
                .thenReturn(false);

        when(dimensionDefinitionRepository.findById(dimensionId1))
                .thenReturn(Optional.of(dimensionDefinition1));

        when(dimensionDefinitionRepository.findById(dimensionId2))
                .thenReturn(Optional.of(dimensionDefinition2));

        when(assessmentRepository.save(any(Assessment.class)))
                .thenReturn(assessment);

        when(dimensionRepository.saveAll(anyList()))
                .thenReturn(Collections.emptyList());

        when(assessmentMapper.toResponseDto(any(Assessment.class)))
                .thenReturn(responseDto);

        // Act
        assessmentService.createAssessment(requestDto, userId);

        // Assert - Should handle zero weight gracefully and return 0
        verify(assessmentRepository).save(argThat(savedAssessment ->
                savedAssessment.getAverageScore() == 0));
    }

    @Test
    void shouldUpdateAssessmentSuccessfully() {
        // Arrange
        Assessment existingAssessment = Assessment.builder()
                .id(assessmentId)
                .userId(userId)
                .reflection("Old reflection")
                .submissionStatus(SubmissionStatus.DRAFT)
                .averageScore(2)
                .build();

        when(assessmentRepository.findById(assessmentId))
                .thenReturn(Optional.of(existingAssessment));

        when(dimensionDefinitionRepository.findExistingIds(List.of(dimensionId1, dimensionId2)))
                .thenReturn(List.of(dimensionId1, dimensionId2));

        when(assessmentRepository.existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
                eq(userId), eq(SubmissionStatus.SUBMITTED), any(LocalDateTime.class)))
                .thenReturn(false);

        when(dimensionDefinitionRepository.findById(dimensionId1))
                .thenReturn(Optional.of(dimensionDefinition1));

        when(dimensionDefinitionRepository.findById(dimensionId2))
                .thenReturn(Optional.of(dimensionDefinition2));

        when(assessmentRepository.save(any(Assessment.class)))
                .thenReturn(assessment);

        when(dimensionRepository.saveAll(anyList()))
                .thenReturn(Collections.emptyList());

        when(assessmentMapper.toResponseDto(any(Assessment.class)))
                .thenReturn(responseDto);

        // Act
        AssessmentResponseDTO result = assessmentService.updateAssessment(assessmentId, requestDto, userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());

        verify(assessmentRepository).findById(assessmentId);
        verify(dimensionRepository).deleteByAssessmentId(assessmentId);
        verify(assessmentRepository).save(any(Assessment.class));
        verify(assessmentEventProducer).publishAssessmentUpdated(any(Assessment.class));

        // Verify dimension definitions were fetched multiple times (for calculation and creation)
        verify(dimensionDefinitionRepository, times(2)).findById(dimensionId1);
        verify(dimensionDefinitionRepository, times(2)).findById(dimensionId2);
    }

    @Test
    void shouldThrowResourceNotFoundWhenUpdateNonExistentAssessment() {
        // Arrange
        when(assessmentRepository.findById(assessmentId))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> assessmentService.updateAssessment(assessmentId, requestDto, userId));

        assertEquals("Assessment with id " + assessmentId + " not found", exception.getMessage());
        verify(assessmentRepository).findById(assessmentId);
        verifyNoMoreInteractions(assessmentRepository, dimensionRepository, assessmentMapper);
    }

    @Test
    void shouldThrowBadRequestWhenUpdateOtherUsersAssessment() {
        // Arrange
        UUID otherUserId = UUID.randomUUID();
        Assessment otherUserAssessment = Assessment.builder()
                .id(assessmentId)
                .userId(otherUserId)
                .reflection("Other user's reflection")
                .submissionStatus(SubmissionStatus.DRAFT)
                .averageScore(3)
                .build();

        when(assessmentRepository.findById(assessmentId))
                .thenReturn(Optional.of(otherUserAssessment));

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> assessmentService.updateAssessment(assessmentId, requestDto, userId));

        assertEquals("You can only update your own assessments", exception.getMessage());
        verify(assessmentRepository).findById(assessmentId);
        verifyNoMoreInteractions(assessmentRepository, dimensionRepository, assessmentMapper);
    }

    @Test
    void shouldHandleEventPublishingFailureGracefully() {
        // Arrange
        when(dimensionDefinitionRepository.findExistingIds(List.of(dimensionId1, dimensionId2)))
                .thenReturn(List.of(dimensionId1, dimensionId2));

        when(assessmentRepository.existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
                eq(userId), eq(SubmissionStatus.SUBMITTED), any(LocalDateTime.class)))
                .thenReturn(false);

        when(dimensionDefinitionRepository.findById(dimensionId1))
                .thenReturn(Optional.of(dimensionDefinition1));

        when(dimensionDefinitionRepository.findById(dimensionId2))
                .thenReturn(Optional.of(dimensionDefinition2));

        when(assessmentRepository.save(any(Assessment.class)))
                .thenReturn(assessment);

        when(dimensionRepository.saveAll(anyList()))
                .thenReturn(Collections.emptyList());

        when(assessmentMapper.toResponseDto(any(Assessment.class)))
                .thenReturn(responseDto);

        // Mock event producer to throw exception
        doThrow(new RuntimeException("Kafka unavailable"))
                .when(assessmentEventProducer).publishAssessmentSubmitted(any(Assessment.class));

        // Act - Should not throw exception despite event publishing failure
        AssessmentResponseDTO result = assessmentService.createAssessment(requestDto, userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());

        // Verify assessment was still saved despite event failure
        verify(assessmentRepository).save(any(Assessment.class));
        verify(assessmentEventProducer).publishAssessmentSubmitted(any(Assessment.class));

        // Verify dimension definitions were fetched multiple times
        verify(dimensionDefinitionRepository, times(2)).findById(dimensionId1);
        verify(dimensionDefinitionRepository, times(2)).findById(dimensionId2);
    }
}