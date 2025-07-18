package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.assessment.request.AssessmentRequestDTO;
import com.talentradar.assessment_service.dto.assessment.request.DimensionRatingDTO;
import com.talentradar.assessment_service.dto.assessment.response.AssessmentResponseDTO;
import com.talentradar.assessment_service.dto.assessment.response.PaginatedResponseDTO;
import com.talentradar.assessment_service.exception.BadRequestException;
import com.talentradar.assessment_service.exception.ResourceNotFoundException;
import com.talentradar.assessment_service.mapper.AssessmentMapper;
import com.talentradar.assessment_service.model.Assessment;
import com.talentradar.assessment_service.model.DimensionDefinition;
import com.talentradar.assessment_service.model.SubmissionStatus;
import com.talentradar.assessment_service.model.UserSnapshot;
import com.talentradar.assessment_service.repository.AssessmentDimensionRepository;
import com.talentradar.assessment_service.repository.AssessmentRepository;
import com.talentradar.assessment_service.repository.DimensionDefinitionRepository;
import com.talentradar.assessment_service.repository.UserSnapshotRepository;
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

    @Mock
    private UserSnapshotRepository userSnapshotRepository;

    private UUID userId;
    private UUID dimensionId1;
    private AssessmentRequestDTO requestDto;
    private Assessment assessment;
    private AssessmentResponseDTO responseDto;
    private DimensionDefinition dimensionDefinition;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        dimensionId1 = UUID.randomUUID();

        requestDto = AssessmentRequestDTO.builder()
                .reflection("Reflection Text")
                .status(SubmissionStatus.SUBMITTED)
                .dimensions(List.of(
                        DimensionRatingDTO.builder()
                                .dimensionDefinitionId(dimensionId1)
                                .rating(4)
                                .build()))
                .build();

        assessment = Assessment.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .reflection("Reflection Text")
                .submissionStatus(SubmissionStatus.SUBMITTED)
                .build();

        responseDto = AssessmentResponseDTO.builder()
                .id(assessment.getId())
                .userId(userId)
                .reflection("Reflection Text")
                .status(SubmissionStatus.SUBMITTED)
                .build();

        dimensionDefinition = DimensionDefinition.builder()
                .id(dimensionId1)
                .dimensionName("Technical Skills")
                .build();
    }

    @Test
    void shouldCreateAssessmentSuccessfully() {
        when(userSnapshotRepository.findById(userId))
                .thenReturn(Optional.of(new UserSnapshot()));

        when(assessmentRepository.existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
                eq(userId),
                eq(SubmissionStatus.SUBMITTED),
                any(LocalDateTime.class)))
                .thenReturn(false);

        when(dimensionDefinitionRepository.findExistingIds(List.of(dimensionId1)))
                .thenReturn(List.of(dimensionId1));

        when(dimensionDefinitionRepository.findById(dimensionId1))
                .thenReturn(Optional.of(dimensionDefinition));

        when(assessmentRepository.save(any(Assessment.class)))
                .thenReturn(assessment);

        when(dimensionRepository.saveAll(anyList()))
                .thenReturn(Collections.emptyList());

        when(assessmentMapper.toResponseDto(any(Assessment.class)))
                .thenReturn(responseDto);

        AssessmentResponseDTO result = assessmentService.createAssessment(requestDto, userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        verify(userSnapshotRepository).findById(userId);
        verify(assessmentRepository).save(any(Assessment.class));
        verify(dimensionRepository).saveAll(anyList());
        verify(assessmentMapper).toResponseDto(any(Assessment.class));
    }

    @Test
    void shouldThrowBadRequestForInvalidDimensionId() {
        when(userSnapshotRepository.findById(userId))
                .thenReturn(Optional.of(new UserSnapshot()));

        when(dimensionDefinitionRepository.findExistingIds(List.of(dimensionId1)))
                .thenReturn(Collections.emptyList());

        assertThrows(BadRequestException.class, () ->
                assessmentService.createAssessment(requestDto, userId));
    }

    @Test
    void shouldThrowResourceNotFoundIfDimensionMissing() {
        when(userSnapshotRepository.findById(userId))
                .thenReturn(Optional.of(new UserSnapshot()));

        when(assessmentRepository.existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
                eq(userId),
                eq(SubmissionStatus.SUBMITTED),
                any(LocalDateTime.class)))
                .thenReturn(false);

        when(dimensionDefinitionRepository.findExistingIds(List.of(dimensionId1)))
                .thenReturn(List.of(dimensionId1));

        when(dimensionDefinitionRepository.findById(dimensionId1))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                assessmentService.createAssessment(requestDto, userId));
    }

    @Test
    void shouldThrowBadRequestIfUserSubmittedWithin30Days() {
        when(userSnapshotRepository.findById(userId))
                .thenReturn(Optional.of(new UserSnapshot()));

        when(dimensionDefinitionRepository.findExistingIds(List.of(dimensionId1)))
                .thenReturn(List.of(dimensionId1));

        when(assessmentRepository.existsByUserIdAndSubmissionStatusAndCreatedAtAfter(
                eq(userId),
                eq(SubmissionStatus.SUBMITTED),
                any(LocalDateTime.class)))
                .thenReturn(true); // simulate 30-day restriction

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> assessmentService.createAssessment(requestDto, userId));

        assertEquals("User has already submitted an assessment within the last 30 days.", exception.getMessage());
    }

    @Test
    void shouldReturnPaginatedAssessmentsByUser() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<Assessment> assessments = List.of(assessment);

        when(userSnapshotRepository.findById(userId))
                .thenReturn(Optional.of(new UserSnapshot()));

        when(assessmentRepository.findAllByUserId(userId, pageable))
                .thenReturn(new PageImpl<>(assessments));

        when(assessmentMapper.toResponseDto(any(Assessment.class)))
                .thenReturn(responseDto);

        // Act
        PaginatedResponseDTO<AssessmentResponseDTO> result = assessmentService.getAssessmentsByUser(userId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(userId, result.getContent().get(0).getUserId());
        verify(assessmentRepository).findAllByUserId(userId, pageable);
        verify(assessmentMapper).toResponseDto(any(Assessment.class));
    }

    @Test
    void shouldThrowResourceNotFoundIfUserDoesNotExistForPagination() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);

        when(userSnapshotRepository.findById(userId))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> assessmentService.getAssessmentsByUser(userId, pageable));

        assertEquals("User not found with ID: " + userId, exception.getMessage());
        verify(userSnapshotRepository).findById(userId);
        verifyNoInteractions(assessmentRepository, assessmentMapper);
    }

}




