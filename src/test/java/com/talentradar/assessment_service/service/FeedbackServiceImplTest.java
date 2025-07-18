package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.dimensionDefinition.request.CreateFeedbackDimensionRequestDto;
import com.talentradar.assessment_service.dto.feedback.request.CreateCompleteFeedbackDto;
import com.talentradar.assessment_service.dto.feedback.response.FeedbackDto;
import com.talentradar.assessment_service.dto.feedbackComment.request.CreateFeedbackCommentRequestDto;
import com.talentradar.assessment_service.dto.feedbackComment.response.FeedbackCommentDto;
import com.talentradar.assessment_service.dto.feedbackDimension.response.FeedbackDimensionDto;
import com.talentradar.assessment_service.exception.FeedbackNotFoundException;
import com.talentradar.assessment_service.model.Feedback;
import com.talentradar.assessment_service.repository.FeedbackRepository;
import com.talentradar.assessment_service.service.FeedbackCommentService;
import com.talentradar.assessment_service.service.FeedbackDimensionService;
import com.talentradar.assessment_service.service.impl.FeedbackServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("FeedbackService Tests")
class FeedbackServiceImplTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private FeedbackDimensionService feedbackDimensionService;

    @Mock
    private FeedbackCommentService feedbackCommentService;

    @InjectMocks
    private FeedbackServiceImpl feedbackService;

    private UUID feedbackId;
    private UUID managerId;
    private UUID developerId;
    private UUID dimensionId;
    private UUID commentId;
    private Feedback sampleFeedback;
    private CreateCompleteFeedbackDto createCompleteFeedbackDto;
    private FeedbackDimensionDto mockDimensionDto;
    private FeedbackCommentDto mockCommentDto;

    @BeforeEach
    void setUp() {
        feedbackId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        developerId = UUID.randomUUID();
        dimensionId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        sampleFeedback = Feedback.builder()
                .id(feedbackId)
                .managerId(managerId)
                .developerId(developerId)
                .feedbackVersion(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Create dimension request
        CreateFeedbackDimensionRequestDto dimensionRequest = CreateFeedbackDimensionRequestDto.builder()
                .dimensionDefinitionId(dimensionId)
                .rating(4)
                .comment("Great technical skills")
                .build();

        // Create comment request
        CreateFeedbackCommentRequestDto commentRequest = CreateFeedbackCommentRequestDto.builder()
                .commentId(commentId)
                .feedbackCommentBody("Excellent performance overall")
                .build();

        createCompleteFeedbackDto = CreateCompleteFeedbackDto.builder()
                .managerId(managerId)
                .developerId(developerId)
                .feedbackVersion(1)
                .dimensions(List.of(dimensionRequest))
                .feedbackComments(List.of(commentRequest))
                .build();

        mockDimensionDto = FeedbackDimensionDto.builder()
                .id(UUID.randomUUID())
                .feedbackId(feedbackId)
                .rating(4)
                .comment("Great technical skills")
                .createdAt(LocalDateTime.now())
                .build();

        mockCommentDto = FeedbackCommentDto.builder()
                .id(UUID.randomUUID())
                .feedbackId(feedbackId)
                .feedbackCommentBody("Excellent performance overall")
                .build();
    }

    @Test
    @DisplayName("Should create complete feedback successfully")
    void createCompleteFeedback_ShouldCreateCompleteFeedback_WhenValidDto() {
        // Given
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(sampleFeedback);
        when(feedbackDimensionService.createFeedbackDimension(any())).thenReturn(mockDimensionDto);
        when(feedbackCommentService.createFeedbackComment(any())).thenReturn(mockCommentDto);

        // When
        FeedbackDto result = feedbackService.createCompleteFeedback(createCompleteFeedbackDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(feedbackId);
        assertThat(result.getManagerId()).isEqualTo(managerId);
        assertThat(result.getDeveloperId()).isEqualTo(developerId);
        assertThat(result.getFeedbackVersion()).isEqualTo(1);
        assertThat(result.getDimensions()).hasSize(1);
        assertThat(result.getFeedbackComments()).hasSize(1);

        verify(feedbackRepository).save(any(Feedback.class));
        verify(feedbackDimensionService).createFeedbackDimension(any());
        verify(feedbackCommentService).createFeedbackComment(any());
    }

    @Test
    @DisplayName("Should get complete feedback successfully")
    void getCompleteFeedback_ShouldReturnCompleteFeedback_WhenIdExists() {
        // Given
        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(sampleFeedback));
        when(feedbackDimensionService.getFeedbackDimensionsByFeedbackId(feedbackId))
                .thenReturn(List.of(mockDimensionDto));
        when(feedbackCommentService.getFeedbackCommentsByFeedbackId(feedbackId))
                .thenReturn(List.of(mockCommentDto));

        // When
        FeedbackDto result = feedbackService.getCompleteFeedback(feedbackId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(feedbackId);
        assertThat(result.getManagerId()).isEqualTo(managerId);
        assertThat(result.getDeveloperId()).isEqualTo(developerId);
        assertThat(result.getDimensions()).hasSize(1);
        assertThat(result.getFeedbackComments()).hasSize(1);

        verify(feedbackRepository).findById(feedbackId);
        verify(feedbackDimensionService).getFeedbackDimensionsByFeedbackId(feedbackId);
        verify(feedbackCommentService).getFeedbackCommentsByFeedbackId(feedbackId);
    }

    @Test
    @DisplayName("Should get feedback by ID successfully")
    void getFeedbackById_ShouldReturnFeedback_WhenIdExists() {
        // Given
        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(sampleFeedback));

        // When
        FeedbackDto result = feedbackService.getFeedbackById(feedbackId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(feedbackId);
        assertThat(result.getManagerId()).isEqualTo(managerId);
        assertThat(result.getDeveloperId()).isEqualTo(developerId);
        assertThat(result.getFeedbackVersion()).isEqualTo(1);
        assertThat(result.getDimensions()).isEmpty(); // Basic DTO without details
        assertThat(result.getFeedbackComments()).isEmpty(); // Basic DTO without details

        verify(feedbackRepository).findById(feedbackId);
    }

    @Test
    @DisplayName("Should throw exception when feedback not found by ID")
    void getFeedbackById_ShouldThrowException_WhenIdNotExists() {
        // Given
        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> feedbackService.getFeedbackById(feedbackId))
                .isInstanceOf(FeedbackNotFoundException.class)
                .hasMessage("Feedback not found with id: " + feedbackId);

        verify(feedbackRepository).findById(feedbackId);
    }

    @Test
    @DisplayName("Should create new feedback version with incremented version")
    void createNewFeedbackVersion_ShouldIncrementVersion_WhenPreviousVersionExists() {
        // Given
        Feedback existingFeedback = Feedback.builder()
                .managerId(managerId)
                .developerId(developerId)
                .feedbackVersion(2)
                .build();

        Feedback newFeedback = Feedback.builder()
                .id(UUID.randomUUID())
                .managerId(managerId)
                .developerId(developerId)
                .feedbackVersion(3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(feedbackRepository.findTopByManagerIdAndDeveloperIdOrderByFeedbackVersionDesc(managerId, developerId))
                .thenReturn(Optional.of(existingFeedback));
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(newFeedback);

        // When
        FeedbackDto result = feedbackService.createNewFeedbackVersion(managerId, developerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getManagerId()).isEqualTo(managerId);
        assertThat(result.getDeveloperId()).isEqualTo(developerId);
        assertThat(result.getFeedbackVersion()).isEqualTo(3);

        verify(feedbackRepository).findTopByManagerIdAndDeveloperIdOrderByFeedbackVersionDesc(managerId, developerId);
        verify(feedbackRepository).save(any(Feedback.class));
    }

    @Test
    @DisplayName("Should create first version when no previous feedback exists")
    void createNewFeedbackVersion_ShouldCreateFirstVersion_WhenNoPreviousFeedbackExists() {
        // Given
        Feedback newFeedback = Feedback.builder()
                .id(UUID.randomUUID())
                .managerId(managerId)
                .developerId(developerId)
                .feedbackVersion(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(feedbackRepository.findTopByManagerIdAndDeveloperIdOrderByFeedbackVersionDesc(managerId, developerId))
                .thenReturn(Optional.empty());
        when(feedbackRepository.save(any(Feedback.class))).thenReturn(newFeedback);

        // When
        FeedbackDto result = feedbackService.createNewFeedbackVersion(managerId, developerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getManagerId()).isEqualTo(managerId);
        assertThat(result.getDeveloperId()).isEqualTo(developerId);
        assertThat(result.getFeedbackVersion()).isEqualTo(1);

        verify(feedbackRepository).findTopByManagerIdAndDeveloperIdOrderByFeedbackVersionDesc(managerId, developerId);
        verify(feedbackRepository).save(any(Feedback.class));
    }

    @Test
    @DisplayName("Should get feedback by manager ID successfully")
    void getFeedbackByManagerId_ShouldReturnFeedbackList_WhenManagerExists() {
        // Given
        List<Feedback> feedbackList = Arrays.asList(sampleFeedback);
        when(feedbackRepository.findByManagerId(managerId)).thenReturn(feedbackList);

        // When
        List<FeedbackDto> result = feedbackService.getFeedbackByManagerId(managerId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getManagerId()).isEqualTo(managerId);
        assertThat(result.get(0).getDeveloperId()).isEqualTo(developerId);

        verify(feedbackRepository).findByManagerId(managerId);
    }

    @Test
    @DisplayName("Should check feedback exists successfully")
    void feedbackExists_ShouldReturnTrue_WhenFeedbackExists() {
        // Given
        when(feedbackRepository.existsById(feedbackId)).thenReturn(true);

        // When
        boolean result = feedbackService.feedbackExists(feedbackId);

        // Then
        assertThat(result).isTrue();
        verify(feedbackRepository).existsById(feedbackId);
    }
}