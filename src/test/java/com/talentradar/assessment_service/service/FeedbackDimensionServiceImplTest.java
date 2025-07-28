package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.dimensionDefinition.response.DimensionDefinitionDto;
import com.talentradar.assessment_service.dto.feedbackDimension.request.CreateFeedbackDimensionDto;
import com.talentradar.assessment_service.dto.feedbackDimension.response.FeedbackDimensionDto;
import com.talentradar.assessment_service.dto.gradingCriteria.response.GradingCriteriaDto;
import com.talentradar.assessment_service.exception.DimensionDefinitionNotFoundException;
import com.talentradar.assessment_service.exception.FeedbackNotFoundException;
import com.talentradar.assessment_service.model.*;
import com.talentradar.assessment_service.repository.DimensionDefinitionRepository;
import com.talentradar.assessment_service.repository.FeedbackDimensionRepository;
import com.talentradar.assessment_service.repository.FeedbackRepository;
import com.talentradar.assessment_service.service.impl.FeedbackDimensionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackDimensionService Tests")
class FeedbackDimensionServiceImplTest {

    @Mock
    private FeedbackDimensionRepository feedbackDimensionRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private DimensionDefinitionRepository dimensionDefinitionRepository;

    @InjectMocks
    private FeedbackDimensionServiceImpl feedbackDimensionService;

    private UUID feedbackId;
    private UUID feedbackDimensionId;
    private UUID dimensionDefinitionId;
    private UUID managerId;
    private UUID developerId;
    private UUID gradingCriteriaId;
    private Feedback sampleFeedback;
    private DimensionDefinition sampleDimensionDefinition;
    private GradingCriteria sampleGradingCriteria;
    private FeedbackDimension sampleFeedbackDimension;
    private CreateFeedbackDimensionDto createDto;

    @BeforeEach
    void setUp() {
        feedbackId = UUID.randomUUID();
        feedbackDimensionId = UUID.randomUUID();
        dimensionDefinitionId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        developerId = UUID.randomUUID();
        gradingCriteriaId = UUID.randomUUID();

        sampleFeedback = Feedback.builder()
                .id(feedbackId)
                .managerId(managerId)
                .developerId(developerId)
                .feedbackVersion(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        sampleGradingCriteria = GradingCriteria.builder()
                .id(gradingCriteriaId)
                .criteriaName("Code Quality")
                .dimensionDefinitions(new HashSet<>())
                .build();

        sampleDimensionDefinition = DimensionDefinition.builder()
                .id(dimensionDefinitionId)
                .dimensionName("Technical Excellence")
                .description("Assessment of technical capabilities")
                .weight(new BigDecimal("25.50"))
                .gradingCriteriaSet(Set.of(sampleGradingCriteria))
                .build();

        sampleFeedbackDimension = FeedbackDimension.builder()
                .id(feedbackDimensionId)
                .feedback(sampleFeedback)
                .dimensionDefinition(sampleDimensionDefinition)
                .rating(4)
                .comment("Excellent technical skills demonstrated")
                .createdAt(LocalDateTime.now())
                .build();

        createDto = CreateFeedbackDimensionDto.builder()
                .feedbackId(feedbackId)
                .dimensionDefinitionId(dimensionDefinitionId)
                .rating(4)
                .comment("Excellent technical skills demonstrated")
                .build();
    }

    @Test
    @DisplayName("Should return false when feedback dimension does not exist for feedback and dimension")
    void feedbackDimensionExistsForFeedbackAndDimension_ShouldReturnFalse_WhenNotExists() {
        // Given
        when(feedbackDimensionRepository.existsByFeedbackIdAndDimensionDefinitionId(feedbackId, dimensionDefinitionId))
                .thenReturn(false);

        // When
        boolean result = feedbackDimensionService.feedbackDimensionExistsForFeedbackAndDimension(feedbackId, dimensionDefinitionId);

        // Then
        assertThat(result).isFalse();

        verify(feedbackDimensionRepository).existsByFeedbackIdAndDimensionDefinitionId(feedbackId, dimensionDefinitionId);
    }

    @Test
    @DisplayName("Should handle multiple feedback dimensions for same feedback")
    void getFeedbackDimensionsByFeedbackId_ShouldReturnMultipleDimensions_WhenMultipleExist() {
        // Given
        UUID secondDimensionId = UUID.randomUUID();
        DimensionDefinition secondDimension = DimensionDefinition.builder()
                .id(secondDimensionId)
                .dimensionName("Communication Skills")
                .description("Assessment of communication abilities")
                .weight(new BigDecimal("20.00"))
                .gradingCriteriaSet(new HashSet<>())
                .build();

        FeedbackDimension secondFeedbackDimension = FeedbackDimension.builder()
                .id(UUID.randomUUID())
                .feedback(sampleFeedback)
                .dimensionDefinition(secondDimension)
                .rating(3)
                .comment("Good communication, needs improvement")
                .createdAt(LocalDateTime.now())
                .build();

        List<FeedbackDimension> multipleDimensions = Arrays.asList(sampleFeedbackDimension, secondFeedbackDimension);
        when(feedbackDimensionRepository.findByFeedbackId(feedbackId)).thenReturn(multipleDimensions);

        // When
        List<FeedbackDimensionDto> result = feedbackDimensionService.getFeedbackDimensionsByFeedbackId(feedbackId);

        // Then
        assertThat(result).hasSize(2);
        
        FeedbackDimensionDto firstResult = result.get(0);
        assertThat(firstResult.getDimensionDefinition().getDimensionName()).isEqualTo("Technical Excellence");
        assertThat(firstResult.getRating()).isEqualTo(4);
        
        FeedbackDimensionDto secondResult = result.get(1);
        assertThat(secondResult.getDimensionDefinition().getDimensionName()).isEqualTo("Communication Skills");
        assertThat(secondResult.getRating()).isEqualTo(3);

        verify(feedbackDimensionRepository).findByFeedbackId(feedbackId);
    }

    @Test
    @DisplayName("Should handle dimension definition with null grading criteria")
    void createFeedbackDimension_ShouldHandleNullGradingCriteria_Gracefully() {
        // Given
        DimensionDefinition dimensionWithNullCriteria = DimensionDefinition.builder()
                .id(dimensionDefinitionId)
                .dimensionName("Technical Excellence")
                .description("Assessment of technical capabilities")
                .weight(new BigDecimal("25.50"))
                .gradingCriteriaSet(null) // Null grading criteria
                .build();

        FeedbackDimension feedbackDimensionWithNullCriteria = FeedbackDimension.builder()
                .id(feedbackDimensionId)
                .feedback(sampleFeedback)
                .dimensionDefinition(dimensionWithNullCriteria)
                .rating(4)
                .comment("Excellent technical skills demonstrated")
                .createdAt(LocalDateTime.now())
                .build();

        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(sampleFeedback));
        when(dimensionDefinitionRepository.findById(dimensionDefinitionId)).thenReturn(Optional.of(dimensionWithNullCriteria));
        when(feedbackDimensionRepository.existsByFeedbackIdAndDimensionDefinitionId(feedbackId, dimensionDefinitionId))
                .thenReturn(false);
        when(feedbackDimensionRepository.save(any(FeedbackDimension.class))).thenReturn(feedbackDimensionWithNullCriteria);

        // When
        FeedbackDimensionDto result = feedbackDimensionService.createFeedbackDimension(createDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDimensionDefinition().getGradingCriteria()).isEmpty();

        verify(feedbackRepository).findById(feedbackId);
        verify(dimensionDefinitionRepository).findById(dimensionDefinitionId);
        verify(feedbackDimensionRepository).save(any(FeedbackDimension.class));
    }

    @Test
    @DisplayName("Should handle dimension definition with empty grading criteria")
    void createFeedbackDimension_ShouldHandleEmptyGradingCriteria_Gracefully() {
        // Given
        DimensionDefinition dimensionWithEmptyCriteria = DimensionDefinition.builder()
                .id(dimensionDefinitionId)
                .dimensionName("Technical Excellence")
                .description("Assessment of technical capabilities")
                .weight(new BigDecimal("25.50"))
                .gradingCriteriaSet(new HashSet<>()) // Empty grading criteria
                .build();

        FeedbackDimension feedbackDimensionWithEmptyCriteria = FeedbackDimension.builder()
                .id(feedbackDimensionId)
                .feedback(sampleFeedback)
                .dimensionDefinition(dimensionWithEmptyCriteria)
                .rating(4)
                .comment("Excellent technical skills demonstrated")
                .createdAt(LocalDateTime.now())
                .build();

        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(sampleFeedback));
        when(dimensionDefinitionRepository.findById(dimensionDefinitionId)).thenReturn(Optional.of(dimensionWithEmptyCriteria));
        when(feedbackDimensionRepository.existsByFeedbackIdAndDimensionDefinitionId(feedbackId, dimensionDefinitionId))
                .thenReturn(false);
        when(feedbackDimensionRepository.save(any(FeedbackDimension.class))).thenReturn(feedbackDimensionWithEmptyCriteria);

        // When
        FeedbackDimensionDto result = feedbackDimensionService.createFeedbackDimension(createDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDimensionDefinition().getGradingCriteria()).isEmpty();

        verify(feedbackRepository).findById(feedbackId);
        verify(dimensionDefinitionRepository).findById(dimensionDefinitionId);
        verify(feedbackDimensionRepository).save(any(FeedbackDimension.class));
    }

    @Test
    @DisplayName("Should correctly map grading criteria to DTOs")
    void createFeedbackDimension_ShouldMapGradingCriteria_Correctly() {
        // Given
        UUID secondCriteriaId = UUID.randomUUID();
        GradingCriteria secondCriteria = GradingCriteria.builder()
                .id(secondCriteriaId)
                .criteriaName("Technical Innovation")
                .dimensionDefinitions(new HashSet<>())
                .build();

        Set<GradingCriteria> multipleCriteria = Set.of(sampleGradingCriteria, secondCriteria);
        
        DimensionDefinition dimensionWithMultipleCriteria = DimensionDefinition.builder()
                .id(dimensionDefinitionId)
                .dimensionName("Technical Excellence")
                .description("Assessment of technical capabilities")
                .weight(new BigDecimal("25.50"))
                .gradingCriteriaSet(multipleCriteria)
                .build();

        FeedbackDimension feedbackDimensionWithMultipleCriteria = FeedbackDimension.builder()
                .id(feedbackDimensionId)
                .feedback(sampleFeedback)
                .dimensionDefinition(dimensionWithMultipleCriteria)
                .rating(4)
                .comment("Excellent technical skills demonstrated")
                .createdAt(LocalDateTime.now())
                .build();

        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(sampleFeedback));
        when(dimensionDefinitionRepository.findById(dimensionDefinitionId)).thenReturn(Optional.of(dimensionWithMultipleCriteria));
        when(feedbackDimensionRepository.existsByFeedbackIdAndDimensionDefinitionId(feedbackId, dimensionDefinitionId))
                .thenReturn(false);
        when(feedbackDimensionRepository.save(any(FeedbackDimension.class))).thenReturn(feedbackDimensionWithMultipleCriteria);

        // When
        FeedbackDimensionDto result = feedbackDimensionService.createFeedbackDimension(createDto);

        // Then
        assertThat(result).isNotNull();
        Set<GradingCriteriaDto> resultCriteria = result.getDimensionDefinition().getGradingCriteria();
        assertThat(resultCriteria).hasSize(2);
        
        List<String> criteriaNames = resultCriteria.stream()
                .map(GradingCriteriaDto::getCriteriaName)
                .toList();
        assertThat(criteriaNames).containsExactlyInAnyOrder("Code Quality", "Technical Innovation");

        verify(feedbackRepository).findById(feedbackId);
        verify(dimensionDefinitionRepository).findById(dimensionDefinitionId);
        verify(feedbackDimensionRepository).save(any(FeedbackDimension.class));
    }

    @Test
    @DisplayName("Should handle extreme rating values correctly")
    void createFeedbackDimension_ShouldHandleExtremeRatings_Correctly() {
        // Given
        CreateFeedbackDimensionDto extremeRatingDto = CreateFeedbackDimensionDto.builder()
                .feedbackId(feedbackId)
                .dimensionDefinitionId(dimensionDefinitionId)
                .rating(5) // Maximum rating
                .comment("Outstanding performance")
                .build();

        FeedbackDimension extremeRatingFeedbackDimension = FeedbackDimension.builder()
                .id(feedbackDimensionId)
                .feedback(sampleFeedback)
                .dimensionDefinition(sampleDimensionDefinition)
                .rating(5)
                .comment("Outstanding performance")
                .createdAt(LocalDateTime.now())
                .build();

        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(sampleFeedback));
        when(dimensionDefinitionRepository.findById(dimensionDefinitionId)).thenReturn(Optional.of(sampleDimensionDefinition));
        when(feedbackDimensionRepository.existsByFeedbackIdAndDimensionDefinitionId(feedbackId, dimensionDefinitionId))
                .thenReturn(false);
        when(feedbackDimensionRepository.save(any(FeedbackDimension.class))).thenReturn(extremeRatingFeedbackDimension);

        // When
        FeedbackDimensionDto result = feedbackDimensionService.createFeedbackDimension(extremeRatingDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getComment()).isEqualTo("Outstanding performance");

        verify(feedbackRepository).findById(feedbackId);
        verify(dimensionDefinitionRepository).findById(dimensionDefinitionId);
        verify(feedbackDimensionRepository).save(any(FeedbackDimension.class));
    }

    @Test
    @DisplayName("Should handle long comment text correctly")
    void createFeedbackDimension_ShouldHandleLongComment_Correctly() {
        // Given
        String longComment = "This is a very long comment that contains detailed feedback about the developer's " +
                           "performance in this dimension. It includes specific examples of work completed, " +
                           "areas where the developer excelled, and suggestions for future improvement. " +
                           "The comment is comprehensive and provides valuable insights for development planning.";
        
        CreateFeedbackDimensionDto longCommentDto = CreateFeedbackDimensionDto.builder()
                .feedbackId(feedbackId)
                .dimensionDefinitionId(dimensionDefinitionId)
                .rating(4)
                .comment(longComment)
                .build();

        FeedbackDimension longCommentFeedbackDimension = FeedbackDimension.builder()
                .id(feedbackDimensionId)
                .feedback(sampleFeedback)
                .dimensionDefinition(sampleDimensionDefinition)
                .rating(4)
                .comment(longComment)
                .createdAt(LocalDateTime.now())
                .build();

        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(sampleFeedback));
        when(dimensionDefinitionRepository.findById(dimensionDefinitionId)).thenReturn(Optional.of(sampleDimensionDefinition));
        when(feedbackDimensionRepository.existsByFeedbackIdAndDimensionDefinitionId(feedbackId, dimensionDefinitionId))
                .thenReturn(false);
        when(feedbackDimensionRepository.save(any(FeedbackDimension.class))).thenReturn(longCommentFeedbackDimension);

        // When
        FeedbackDimensionDto result = feedbackDimensionService.createFeedbackDimension(longCommentDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getComment()).isEqualTo(longComment);
        assertThat(result.getComment().length()).isGreaterThan(200);

        verify(feedbackRepository).findById(feedbackId);
        verify(dimensionDefinitionRepository).findById(dimensionDefinitionId);
        verify(feedbackDimensionRepository).save(any(FeedbackDimension.class));
    }

    @Test
    @DisplayName("Should handle repository save failure gracefully")
    void createFeedbackDimension_ShouldHandleRepositoryFailure_Gracefully() {
        // Given
        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(sampleFeedback));
        when(dimensionDefinitionRepository.findById(dimensionDefinitionId)).thenReturn(Optional.of(sampleDimensionDefinition));
        when(feedbackDimensionRepository.existsByFeedbackIdAndDimensionDefinitionId(feedbackId, dimensionDefinitionId))
                .thenReturn(false);
        when(feedbackDimensionRepository.save(any(FeedbackDimension.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        assertThatThrownBy(() -> feedbackDimensionService.createFeedbackDimension(createDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Database connection failed");

        verify(feedbackRepository).findById(feedbackId);
        verify(dimensionDefinitionRepository).findById(dimensionDefinitionId);
        verify(feedbackDimensionRepository).existsByFeedbackIdAndDimensionDefinitionId(feedbackId, dimensionDefinitionId);
        verify(feedbackDimensionRepository).save(any(FeedbackDimension.class));
    }

    @Test
    @DisplayName("Should validate all required fields are present in saved entity")
    void createFeedbackDimension_ShouldValidateEntityFields_BeforeSaving() {
        // Given
        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(sampleFeedback));
        when(dimensionDefinitionRepository.findById(dimensionDefinitionId)).thenReturn(Optional.of(sampleDimensionDefinition));
        when(feedbackDimensionRepository.existsByFeedbackIdAndDimensionDefinitionId(feedbackId, dimensionDefinitionId))
                .thenReturn(false);
        when(feedbackDimensionRepository.save(any(FeedbackDimension.class))).thenReturn(sampleFeedbackDimension);

        // When
        FeedbackDimensionDto result = feedbackDimensionService.createFeedbackDimension(createDto);

        // Then
        assertThat(result).isNotNull();
        
        // Verify that save was called with correct entity structure
        verify(feedbackDimensionRepository).save(argThat(feedbackDimension -> {
            assertThat(feedbackDimension.getFeedback()).isNotNull();
            assertThat(feedbackDimension.getFeedback().getId()).isEqualTo(feedbackId);
            assertThat(feedbackDimension.getDimensionDefinition()).isNotNull();
            assertThat(feedbackDimension.getDimensionDefinition().getId()).isEqualTo(dimensionDefinitionId);
            assertThat(feedbackDimension.getRating()).isEqualTo(4);
            assertThat(feedbackDimension.getComment()).isEqualTo("Excellent technical skills demonstrated");
            return true;
        }));
    }
}