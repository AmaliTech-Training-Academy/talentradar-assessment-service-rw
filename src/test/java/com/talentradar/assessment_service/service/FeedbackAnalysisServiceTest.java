package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.analysis.FeedbackAnalysisDto;
import com.talentradar.assessment_service.dto.comment.response.CommentDto;
import com.talentradar.assessment_service.dto.dimensionDefinition.response.DimensionDefinitionDto;
import com.talentradar.assessment_service.dto.feedbackComment.response.FeedbackCommentDto;
import com.talentradar.assessment_service.dto.feedbackDimension.response.FeedbackDimensionDto;
import com.talentradar.assessment_service.dto.gradingCriteria.response.GradingCriteriaDto;
import com.talentradar.assessment_service.model.*;
import com.talentradar.assessment_service.repository.AssessmentRepository;
import com.talentradar.assessment_service.service.FeedbackCommentService;
import com.talentradar.assessment_service.service.FeedbackDimensionService;
import com.talentradar.assessment_service.service.impl.FeedbackAnalysisService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackAnalysisService Tests")
class FeedbackAnalysisServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private FeedbackDimensionService feedbackDimensionService;

    @Mock
    private FeedbackCommentService feedbackCommentService;

    @InjectMocks
    private FeedbackAnalysisService feedbackAnalysisService;

    private UUID feedbackId;
    private UUID managerId;
    private UUID developerId;
    private UUID assessmentId;
    private UUID dimensionDefinitionId1;
    private UUID dimensionDefinitionId2;
    private Feedback sampleFeedback;
    private Assessment sampleAssessment;
    private DimensionDefinition technicalDimension;
    private DimensionDefinition communicationDimension;
    private AssessmentDimension assessmentDimension1;
    private AssessmentDimension assessmentDimension2;

    @BeforeEach
    void setUp() {
        feedbackId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        developerId = UUID.randomUUID();
        assessmentId = UUID.randomUUID();
        dimensionDefinitionId1 = UUID.randomUUID();
        dimensionDefinitionId2 = UUID.randomUUID();

        sampleFeedback = Feedback.builder()
                .id(feedbackId)
                .managerId(managerId)
                .developerId(developerId)
                .feedbackVersion(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        technicalDimension = DimensionDefinition.builder()
                .id(dimensionDefinitionId1)
                .dimensionName("Technical Excellence")
                .description("Technical skills assessment")
                .weight(new BigDecimal("25.0"))
                .gradingCriteriaSet(new HashSet<>())
                .build();

        communicationDimension = DimensionDefinition.builder()
                .id(dimensionDefinitionId2)
                .dimensionName("Communication & Collaboration")
                .description("Communication skills assessment")
                .weight(new BigDecimal("20.0"))
                .gradingCriteriaSet(new HashSet<>())
                .build();

        assessmentDimension1 = AssessmentDimension.builder()
                .id(UUID.randomUUID())
                .dimensionDefinition(technicalDimension)
                .rating(4)
                .createdAt(LocalDateTime.now())
                .build();

        assessmentDimension2 = AssessmentDimension.builder()
                .id(UUID.randomUUID())
                .dimensionDefinition(communicationDimension)
                .rating(3)
                .createdAt(LocalDateTime.now())
                .build();

        sampleAssessment = Assessment.builder()
                .id(assessmentId)
                .userId(developerId)
                .reflection("I believe I have strong technical skills and good communication abilities.")
                .submissionStatus(SubmissionStatus.SUBMITTED)
                .averageScore(4)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .dimensions(Arrays.asList(assessmentDimension1, assessmentDimension2))
                .build();
    }

    @Test
    @DisplayName("Should create complete analysis DTO with both self-assessment and manager feedback")
    void createAnalysisDto_ShouldCreateCompleteAnalysis_WhenBothAssessmentAndFeedbackExist() {
        // Given
        when(assessmentRepository.findLatestSubmittedAssessmentByUserId(developerId))
                .thenReturn(Optional.of(sampleAssessment));

        // Mock feedback dimensions
        List<FeedbackDimensionDto> feedbackDimensions = createMockFeedbackDimensions();
        when(feedbackDimensionService.getFeedbackDimensionsByFeedbackId(feedbackId))
                .thenReturn(feedbackDimensions);

        // Mock feedback comments
        List<FeedbackCommentDto> feedbackComments = createMockFeedbackComments();
        when(feedbackCommentService.getFeedbackCommentsByFeedbackId(feedbackId))
                .thenReturn(feedbackComments);

        // When
        FeedbackAnalysisDto result = feedbackAnalysisService.createAnalysisDto(sampleFeedback);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(developerId.toString());

        // Verify self-assessment data is null
        assertThat(result.getSelfAssessment()).isNull();

        // Verify manager feedback data exists
        FeedbackAnalysisDto.ManagerFeedbackData managerFeedback = result.getManagerFeedback();
        assertThat(managerFeedback).isNotNull();
        assertThat(managerFeedback.getScores()).hasSize(2);
        assertThat(managerFeedback.getScores().get("technicalexcellence")).isEqualTo(5);
        assertThat(managerFeedback.getScores().get("communicationcollaboration")).isEqualTo(4);

        verify(assessmentRepository).findLatestSubmittedAssessmentByUserId(developerId);
        verify(feedbackDimensionService).getFeedbackDimensionsByFeedbackId(feedbackId);
        verify(feedbackCommentService).getFeedbackCommentsByFeedbackId(feedbackId);
    }

    @Test
    @DisplayName("Should handle empty feedback dimensions gracefully")
    void createAnalysisDto_ShouldHandleEmptyDimensions_WhenNoFeedbackDimensionsExist() {
        // Given
        when(assessmentRepository.findLatestSubmittedAssessmentByUserId(developerId))
                .thenReturn(Optional.of(sampleAssessment));

        // Mock empty feedback dimensions
        when(feedbackDimensionService.getFeedbackDimensionsByFeedbackId(feedbackId))
                .thenReturn(Collections.emptyList());

        // Mock empty feedback comments
        when(feedbackCommentService.getFeedbackCommentsByFeedbackId(feedbackId))
                .thenReturn(Collections.emptyList());

        // When
        FeedbackAnalysisDto result = feedbackAnalysisService.createAnalysisDto(sampleFeedback);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(developerId.toString());

        // Verify self-assessment data exists
        FeedbackAnalysisDto.SelfAssessmentData selfAssessment = result.getSelfAssessment();
        assertThat(selfAssessment).isNotNull();
        assertThat(selfAssessment.getScores()).hasSize(2);

        // Verify manager feedback data exists but with empty scores and reflection
        FeedbackAnalysisDto.ManagerFeedbackData managerFeedback = result.getManagerFeedback();
        assertThat(managerFeedback).isNotNull();
        assertThat(managerFeedback.getScores()).isEmpty();
        assertThat(managerFeedback.getReflection()).isEmpty();

        verify(assessmentRepository).findLatestSubmittedAssessmentByUserId(developerId);
        verify(feedbackDimensionService).getFeedbackDimensionsByFeedbackId(feedbackId);
        verify(feedbackCommentService).getFeedbackCommentsByFeedbackId(feedbackId);
    }

    @Test
    @DisplayName("Should handle assessment with null dimensions")
    void createAnalysisDto_ShouldHandleNullAssessmentDimensions_Gracefully() {
        // Given
        Assessment assessmentWithNullDimensions = Assessment.builder()
                .id(assessmentId)
                .userId(developerId)
                .reflection("Test reflection")
                .submissionStatus(SubmissionStatus.SUBMITTED)
                .averageScore(3)
                .dimensions(null) // Null dimensions
                .build();

        when(assessmentRepository.findLatestSubmittedAssessmentByUserId(developerId))
                .thenReturn(Optional.of(assessmentWithNullDimensions));

        // Mock feedback data
        List<FeedbackDimensionDto> feedbackDimensions = createMockFeedbackDimensions();
        when(feedbackDimensionService.getFeedbackDimensionsByFeedbackId(feedbackId))
                .thenReturn(feedbackDimensions);

        List<FeedbackCommentDto> feedbackComments = createMockFeedbackComments();
        when(feedbackCommentService.getFeedbackCommentsByFeedbackId(feedbackId))
                .thenReturn(feedbackComments);

        // When
        FeedbackAnalysisDto result = feedbackAnalysisService.createAnalysisDto(sampleFeedback);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(developerId.toString());

        // Verify self-assessment data exists but with empty scores
        FeedbackAnalysisDto.SelfAssessmentData selfAssessment = result.getSelfAssessment();
        assertThat(selfAssessment).isNotNull();
        assertThat(selfAssessment.getReflection()).isEqualTo("Test reflection");
        assertThat(selfAssessment.getScores()).isEmpty();

        // Verify manager feedback data exists
        FeedbackAnalysisDto.ManagerFeedbackData managerFeedback = result.getManagerFeedback();
        assertThat(managerFeedback).isNotNull();
        assertThat(managerFeedback.getScores()).hasSize(2);

        verify(assessmentRepository).findLatestSubmittedAssessmentByUserId(developerId);
        verify(feedbackDimensionService).getFeedbackDimensionsByFeedbackId(feedbackId);
        verify(feedbackCommentService).getFeedbackCommentsByFeedbackId(feedbackId);
    }

    @Test
    @DisplayName("Should normalize dimension names correctly")
    void createAnalysisDto_ShouldNormalizeDimensionNames_Correctly() {
        // Given
        // Create dimensions with special characters and spaces
        DimensionDefinition complexNameDimension = DimensionDefinition.builder()
                .id(dimensionDefinitionId1)
                .dimensionName("Team Dynamics & Leadership")
                .description("Team dynamics assessment")
                .weight(new BigDecimal("20.0"))
                .build();

        AssessmentDimension complexAssessmentDimension = AssessmentDimension.builder()
                .id(UUID.randomUUID())
                .dimensionDefinition(complexNameDimension)
                .rating(5)
                .build();

        Assessment assessmentWithComplexNames = Assessment.builder()
                .id(assessmentId)
                .userId(developerId)
                .reflection("Test reflection")
                .submissionStatus(SubmissionStatus.SUBMITTED)
                .averageScore(5)
                .dimensions(Arrays.asList(complexAssessmentDimension))
                .build();

        when(assessmentRepository.findLatestSubmittedAssessmentByUserId(developerId))
                .thenReturn(Optional.of(assessmentWithComplexNames));

        // Mock feedback dimension with same complex name
        DimensionDefinitionDto complexFeedbackDimension = DimensionDefinitionDto.builder()
                .id(dimensionDefinitionId1)
                .dimensionName("Team Dynamics & Leadership")
                .description("Team dynamics assessment")
                .weight(new BigDecimal("20.0"))
                .gradingCriteria(new HashSet<>())
                .build();

        FeedbackDimensionDto feedbackDimensionDto = FeedbackDimensionDto.builder()
                .id(UUID.randomUUID())
                .feedbackId(feedbackId)
                .dimensionDefinition(complexFeedbackDimension)
                .rating(4)
                .comment("Good team leadership")
                .createdAt(LocalDateTime.now())
                .build();

        when(feedbackDimensionService.getFeedbackDimensionsByFeedbackId(feedbackId))
                .thenReturn(Arrays.asList(feedbackDimensionDto));

        when(feedbackCommentService.getFeedbackCommentsByFeedbackId(feedbackId))
                .thenReturn(Collections.emptyList());

        // When
        FeedbackAnalysisDto result = feedbackAnalysisService.createAnalysisDto(sampleFeedback);

        // Then
        assertThat(result).isNotNull();

        // Verify dimension name normalization (spaces and & removed, lowercase)
        String expectedNormalizedName = "teamdynamicsleadership";
        
        FeedbackAnalysisDto.SelfAssessmentData selfAssessment = result.getSelfAssessment();
        assertThat(selfAssessment.getScores()).containsKey(expectedNormalizedName);
        assertThat(selfAssessment.getScores().get(expectedNormalizedName)).isEqualTo(5);

        FeedbackAnalysisDto.ManagerFeedbackData managerFeedback = result.getManagerFeedback();
        assertThat(managerFeedback.getScores()).containsKey(expectedNormalizedName);
        assertThat(managerFeedback.getScores().get(expectedNormalizedName)).isEqualTo(4);
    }

    @Test
    @DisplayName("Should combine multiple feedback comments correctly")
    void createAnalysisDto_ShouldCombineFeedbackComments_Correctly() {
        // Given
        when(assessmentRepository.findLatestSubmittedAssessmentByUserId(developerId))
                .thenReturn(Optional.of(sampleAssessment));

        when(feedbackDimensionService.getFeedbackDimensionsByFeedbackId(feedbackId))
                .thenReturn(Collections.emptyList());

        // Mock multiple feedback comments
        List<FeedbackCommentDto> multipleComments = Arrays.asList(
                createFeedbackComment("Key Strengths", "Excellent problem-solving skills"),
                createFeedbackComment("Development Areas", "Could improve time management"),
                createFeedbackComment("Goals", "Focus on leadership development")
        );

        when(feedbackCommentService.getFeedbackCommentsByFeedbackId(feedbackId))
                .thenReturn(multipleComments);

        // When
        FeedbackAnalysisDto result = feedbackAnalysisService.createAnalysisDto(sampleFeedback);

        // Then
        assertThat(result).isNotNull();
        
        FeedbackAnalysisDto.ManagerFeedbackData managerFeedback = result.getManagerFeedback();
        assertThat(managerFeedback).isNotNull();
        
        String expectedReflection = "Key Strengths: Excellent problem-solving skills | " +
                                  "Development Areas: Could improve time management | " +
                                  "Goals: Focus on leadership development";
        assertThat(managerFeedback.getReflection()).isEqualTo(expectedReflection);
    }

    @Test
    @DisplayName("Should handle service method failures gracefully")
    void createAnalysisDto_ShouldHandleServiceFailures_Gracefully() {
        // Given
        when(assessmentRepository.findLatestSubmittedAssessmentByUserId(developerId))
                .thenReturn(Optional.of(sampleAssessment));

        // Mock service to throw exception
        when(feedbackDimensionService.getFeedbackDimensionsByFeedbackId(feedbackId))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When & Then
        assertThatThrownBy(() -> feedbackAnalysisService.createAnalysisDto(sampleFeedback))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Service unavailable");

        verify(assessmentRepository).findLatestSubmittedAssessmentByUserId(developerId);
        verify(feedbackDimensionService).getFeedbackDimensionsByFeedbackId(feedbackId);
    }

    @Test
    @DisplayName("Should handle large number of dimensions efficiently")
    void createAnalysisDto_ShouldHandleLargeDimensionSet_Efficiently() {
        // Given
        List<AssessmentDimension> manyAssessmentDimensions = new ArrayList<>();
        List<FeedbackDimensionDto> manyFeedbackDimensions = new ArrayList<>();

        // Create 10 dimensions for testing
        for (int i = 0; i < 10; i++) {
            DimensionDefinition dimension = DimensionDefinition.builder()
                    .id(UUID.randomUUID())
                    .dimensionName("Dimension " + i)
                    .description("Description " + i)
                    .weight(new BigDecimal("10.0"))
                    .build();

            AssessmentDimension assessmentDim = AssessmentDimension.builder()
                    .id(UUID.randomUUID())
                    .dimensionDefinition(dimension)
                    .rating(i % 5 + 1) // Ratings 1-5
                    .build();
            manyAssessmentDimensions.add(assessmentDim);

            // Create corresponding feedback dimension
            DimensionDefinitionDto dimensionDto = DimensionDefinitionDto.builder()
                    .id(dimension.getId())
                    .dimensionName(dimension.getDimensionName())
                    .description(dimension.getDescription())
                    .weight(dimension.getWeight())
                    .gradingCriteria(new HashSet<>())
                    .build();

            FeedbackDimensionDto feedbackDim = FeedbackDimensionDto.builder()
                    .id(UUID.randomUUID())
                    .feedbackId(feedbackId)
                    .dimensionDefinition(dimensionDto)
                    .rating((i % 5) + 1)
                    .comment("Comment " + i)
                    .createdAt(LocalDateTime.now())
                    .build();
            manyFeedbackDimensions.add(feedbackDim);
        }

        Assessment largeDimensionAssessment = Assessment.builder()
                .id(assessmentId)
                .userId(developerId)
                .reflection("Large dimension test")
                .submissionStatus(SubmissionStatus.SUBMITTED)
                .averageScore(3)
                .dimensions(manyAssessmentDimensions)
                .build();

        when(assessmentRepository.findLatestSubmittedAssessmentByUserId(developerId))
                .thenReturn(Optional.of(largeDimensionAssessment));

        when(feedbackDimensionService.getFeedbackDimensionsByFeedbackId(feedbackId))
                .thenReturn(manyFeedbackDimensions);

        when(feedbackCommentService.getFeedbackCommentsByFeedbackId(feedbackId))
                .thenReturn(Collections.emptyList());

        // When
        FeedbackAnalysisDto result = feedbackAnalysisService.createAnalysisDto(sampleFeedback);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSelfAssessment().getScores()).hasSize(10);
        assertThat(result.getManagerFeedback().getScores()).hasSize(10);

        // Verify all dimensions are present with correct normalized names
        for (int i = 0; i < 10; i++) {
            String normalizedName = ("dimension" + i).toLowerCase().replaceAll("\\s+", "");
            assertThat(result.getSelfAssessment().getScores()).containsKey(normalizedName);
            assertThat(result.getManagerFeedback().getScores()).containsKey(normalizedName);
        }
    }

    // Helper methods
    private List<FeedbackDimensionDto> createMockFeedbackDimensions() {
        DimensionDefinitionDto techDimensionDto = DimensionDefinitionDto.builder()
                .id(dimensionDefinitionId1)
                .dimensionName("Technical Excellence")
                .description("Technical skills assessment")
                .weight(new BigDecimal("25.0"))
                .gradingCriteria(new HashSet<>())
                .build();

        DimensionDefinitionDto commDimensionDto = DimensionDefinitionDto.builder()
                .id(dimensionDefinitionId2)
                .dimensionName("Communication & Collaboration")
                .description("Communication skills assessment")
                .weight(new BigDecimal("20.0"))
                .gradingCriteria(new HashSet<>())
                .build();

        FeedbackDimensionDto feedbackDim1 = FeedbackDimensionDto.builder()
                .id(UUID.randomUUID())
                .feedbackId(feedbackId)
                .dimensionDefinition(techDimensionDto)
                .rating(5)
                .comment("Excellent technical skills")
                .createdAt(LocalDateTime.now())
                .build();

        FeedbackDimensionDto feedbackDim2 = FeedbackDimensionDto.builder()
                .id(UUID.randomUUID())
                .feedbackId(feedbackId)
                .dimensionDefinition(commDimensionDto)
                .rating(4)
                .comment("Good communication abilities")
                .createdAt(LocalDateTime.now())
                .build();

        return Arrays.asList(feedbackDim1, feedbackDim2);
    }

    private List<FeedbackCommentDto> createMockFeedbackComments() {
        return Arrays.asList(
                createFeedbackComment("Key Strengths", "Excellent technical skills and problem-solving ability"),
                createFeedbackComment("Development Areas", "Could improve communication in team meetings")
        );
    }

    private FeedbackCommentDto createFeedbackComment(String title, String body) {
        CommentDto commentDto = CommentDto.builder()
                .id(UUID.randomUUID())
                .commentTitle(title)
                .build();

        return FeedbackCommentDto.builder()
                .id(UUID.randomUUID())
                .feedbackId(feedbackId)
                .comment(commentDto)
                .feedbackCommentBody(body)
                .build();
    }
}