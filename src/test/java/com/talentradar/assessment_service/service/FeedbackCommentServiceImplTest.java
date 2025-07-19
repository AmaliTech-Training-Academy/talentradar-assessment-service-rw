package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.comment.response.CommentDto;
import com.talentradar.assessment_service.dto.feedbackComment.request.CreateFeedbackCommentDto;
import com.talentradar.assessment_service.dto.feedbackComment.response.FeedbackCommentDto;
import com.talentradar.assessment_service.exception.CommentNotFoundException;
import com.talentradar.assessment_service.exception.FeedbackNotFoundException;
import com.talentradar.assessment_service.model.Comment;
import com.talentradar.assessment_service.model.Feedback;
import com.talentradar.assessment_service.model.FeedbackComment;
import com.talentradar.assessment_service.repository.CommentRepository;
import com.talentradar.assessment_service.repository.FeedbackCommentRepository;
import com.talentradar.assessment_service.repository.FeedbackRepository;
import com.talentradar.assessment_service.service.impl.FeedbackCommentServiceImpl;
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
@DisplayName("FeedbackCommentService Tests")
class FeedbackCommentServiceImplTest {

    @Mock
    private FeedbackCommentRepository feedbackCommentRepository;

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private FeedbackCommentServiceImpl feedbackCommentService;

    private UUID feedbackCommentId;
    private UUID feedbackId;
    private UUID commentId;
    private UUID managerId;
    private UUID developerId;
    private Feedback sampleFeedback;
    private Comment sampleComment;
    private FeedbackComment sampleFeedbackComment;
    private CreateFeedbackCommentDto createDto;

    @BeforeEach
    void setUp() {
        feedbackCommentId = UUID.randomUUID();
        feedbackId = UUID.randomUUID();
        commentId = UUID.randomUUID();
        managerId = UUID.randomUUID();
        developerId = UUID.randomUUID();

        sampleFeedback = Feedback.builder()
                .id(feedbackId)
                .managerId(managerId)
                .developerId(developerId)
                .feedbackVersion(1)
                .build();

        sampleComment = Comment.builder()
                .id(commentId)
                .commentTitle("Key Strengths & Achievements")
                .build();

        sampleFeedbackComment = FeedbackComment.builder()
                .id(feedbackCommentId)
                .feedback(sampleFeedback)
                .comment(sampleComment)
                .feedbackCommentBody("Demonstrates excellent problem-solving skills and leadership qualities")
                .build();

        createDto = CreateFeedbackCommentDto.builder()
                .feedbackId(feedbackId)
                .commentId(commentId)
                .feedbackCommentBody("Demonstrates excellent problem-solving skills and leadership qualities")
                .build();
    }

    @Test
    @DisplayName("Should create feedback comment successfully")
    void createFeedbackComment_ShouldCreateFeedbackComment_WhenValidDto() {
        // Given
        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(sampleFeedback));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(sampleComment));
        when(feedbackCommentRepository.save(any(FeedbackComment.class))).thenReturn(sampleFeedbackComment);

        // When
        FeedbackCommentDto result = feedbackCommentService.createFeedbackComment(createDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(feedbackCommentId);
        assertThat(result.getFeedbackId()).isEqualTo(feedbackId);
        assertThat(result.getComment().getId()).isEqualTo(commentId);
        assertThat(result.getComment().getCommentTitle()).isEqualTo("Key Strengths & Achievements");
        assertThat(result.getFeedbackCommentBody()).isEqualTo("Demonstrates excellent problem-solving skills and leadership qualities");

        verify(feedbackRepository).findById(feedbackId);
        verify(commentRepository).findById(commentId);
        verify(feedbackCommentRepository).save(any(FeedbackComment.class));
    }

    @Test
    @DisplayName("Should throw exception when feedback not found")
    void createFeedbackComment_ShouldThrowException_WhenFeedbackNotExists() {
        // Given
        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> feedbackCommentService.createFeedbackComment(createDto))
                .isInstanceOf(FeedbackNotFoundException.class)
                .hasMessage("Feedback not found with id: " + feedbackId);

        verify(feedbackRepository).findById(feedbackId);
        verify(commentRepository, never()).findById(any());
        verify(feedbackCommentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when comment not found")
    void createFeedbackComment_ShouldThrowException_WhenCommentNotExists() {
        // Given
        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(sampleFeedback));
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> feedbackCommentService.createFeedbackComment(createDto))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage("Comment not found with id: " + commentId);

        verify(feedbackRepository).findById(feedbackId);
        verify(commentRepository).findById(commentId);
        verify(feedbackCommentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get feedback comments by feedback ID successfully")
    void getFeedbackCommentsByFeedbackId_ShouldReturnComments_WhenFeedbackExists() {
        // Given
        FeedbackComment mockFeedbackComment = FeedbackComment.builder().id(feedbackId).build();
        List<FeedbackComment> feedbackComments = Arrays.asList(sampleFeedbackComment);
        when(feedbackCommentRepository.findByFeedbackId(mockFeedbackComment)).thenReturn(feedbackComments);

        // When
        List<FeedbackCommentDto> result = feedbackCommentService.getFeedbackCommentsByFeedbackId(feedbackId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(feedbackCommentId);
        assertThat(result.get(0).getFeedbackId()).isEqualTo(feedbackId);
        assertThat(result.get(0).getComment().getId()).isEqualTo(commentId);
        assertThat(result.get(0).getComment().getCommentTitle()).isEqualTo("Key Strengths & Achievements");
        assertThat(result.get(0).getFeedbackCommentBody()).isEqualTo("Demonstrates excellent problem-solving skills and leadership qualities");

        verify(feedbackCommentRepository).findByFeedbackId(any(FeedbackComment.class));
    }

    @Test
    @DisplayName("Should return empty list when no feedback comments exist")
    void getFeedbackCommentsByFeedbackId_ShouldReturnEmptyList_WhenNoCommentsExist() {
        // Given
        FeedbackComment mockFeedbackComment = FeedbackComment.builder().id(feedbackId).build();
        when(feedbackCommentRepository.findByFeedbackId(mockFeedbackComment)).thenReturn(Collections.emptyList());

        // When
        List<FeedbackCommentDto> result = feedbackCommentService.getFeedbackCommentsByFeedbackId(feedbackId);

        // Then
        assertThat(result).isEmpty();
        verify(feedbackCommentRepository).findByFeedbackId(any(FeedbackComment.class));
    }

    @Test
    @DisplayName("Should map feedback comment to DTO correctly")
    void mapToDto_ShouldMapCorrectly_WhenValidFeedbackComment() {

        when(feedbackRepository.findById(feedbackId)).thenReturn(Optional.of(sampleFeedback));
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(sampleComment));
        when(feedbackCommentRepository.save(any(FeedbackComment.class))).thenReturn(sampleFeedbackComment);

        FeedbackCommentDto result = feedbackCommentService.createFeedbackComment(createDto);

        assertThat(result.getId()).isEqualTo(feedbackCommentId);
        assertThat(result.getFeedbackId()).isEqualTo(feedbackId);
        assertThat(result.getFeedbackCommentBody()).isEqualTo("Demonstrates excellent problem-solving skills and leadership qualities");
        
        assertThat(result.getComment()).isNotNull();
        assertThat(result.getComment().getId()).isEqualTo(commentId);
        assertThat(result.getComment().getCommentTitle()).isEqualTo("Key Strengths & Achievements");
    }

    @Test
    @DisplayName("Should handle null feedback comments list gracefully")
    void getFeedbackCommentsByFeedbackId_ShouldHandleNullGracefully() {
        FeedbackComment mockFeedbackComment = FeedbackComment.builder().id(feedbackId).build();
        when(feedbackCommentRepository.findByFeedbackId(mockFeedbackComment)).thenReturn(null);

        assertThatThrownBy(() -> feedbackCommentService.getFeedbackCommentsByFeedbackId(feedbackId))
                .isInstanceOf(NullPointerException.class);

        verify(feedbackCommentRepository).findByFeedbackId(any(FeedbackComment.class));
    }
}