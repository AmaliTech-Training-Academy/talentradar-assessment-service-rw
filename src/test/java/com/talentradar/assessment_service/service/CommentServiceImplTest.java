package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.comment.CommentDto;
import com.talentradar.assessment_service.dto.comment.CreateCommentDto;
import com.talentradar.assessment_service.dto.comment.UpdateCommentDto;
import com.talentradar.assessment_service.exception.CommentNotFoundException;
import com.talentradar.assessment_service.model.Comment;
import com.talentradar.assessment_service.repository.CommentRepository;
import com.talentradar.assessment_service.service.impl.CommentServiceImpl;
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
@DisplayName("CommentService Tests")
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Comment sampleComment;
    private CreateCommentDto createDto;
    private UpdateCommentDto updateDto;
    private UUID commentId;

    @BeforeEach
    void setUp() {
        commentId = UUID.randomUUID();
        
        sampleComment = Comment.builder()
                .id(commentId)
                .commentTitle("Excellent problem-solving skills")
                .build();

        createDto = CreateCommentDto.builder()
                .commentTitle("Great communication abilities")
                .build();

        updateDto = UpdateCommentDto.builder()
                .commentTitle("Updated: Outstanding technical expertise")
                .build();
    }

    @Test
    @DisplayName("Should return all comments successfully")
    void getAllComments_ShouldReturnAllComments() {
        // Given
        List<Comment> comments = Arrays.asList(sampleComment);
        when(commentRepository.findAll()).thenReturn(comments);

        // When
        List<CommentDto> result = commentService.getAllComments();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(commentId);
        assertThat(result.get(0).getCommentTitle()).isEqualTo("Excellent problem-solving skills");
        
        verify(commentRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no comments exist")
    void getAllComments_ShouldReturnEmptyList_WhenNoCommentsExist() {
        // Given
        when(commentRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<CommentDto> result = commentService.getAllComments();

        // Then
        assertThat(result).isEmpty();
        verify(commentRepository).findAll();
    }

    @Test
    @DisplayName("Should return comment by ID successfully")
    void getCommentById_ShouldReturnComment_WhenIdExists() {
        // Given
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(sampleComment));

        // When
        CommentDto result = commentService.getCommentById(commentId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(commentId);
        assertThat(result.getCommentTitle()).isEqualTo("Excellent problem-solving skills");
        
        verify(commentRepository).findById(commentId);
    }

    @Test
    @DisplayName("Should throw exception when comment not found by ID")
    void getCommentById_ShouldThrowException_WhenIdNotExists() {
        // Given
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.getCommentById(commentId))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage("Comment not found with id: " + commentId);
        
        verify(commentRepository).findById(commentId);
    }

    @Test
    @DisplayName("Should create comment successfully")
    void createComment_ShouldCreateComment_WhenValidDto() {
        // Given
        Comment newComment = Comment.builder()
                .id(UUID.randomUUID())
                .commentTitle(createDto.getCommentTitle())
                .build();
        
        when(commentRepository.save(any(Comment.class))).thenReturn(newComment);

        // When
        CommentDto result = commentService.createComment(createDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCommentTitle()).isEqualTo("Great communication abilities");
        
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    @DisplayName("Should update comment successfully when comment exists")
    void updateComment_ShouldUpdateComment_WhenIdExists() {
        // Given
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(sampleComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(sampleComment);

        // When
        CommentDto result = commentService.updateComment(commentId, updateDto);

        // Then
        assertThat(result).isNotNull();
        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(sampleComment);
        
        // Verify the comment was updated
        assertThat(sampleComment.getCommentTitle()).isEqualTo("Updated: Outstanding technical expertise");
    }

    @Test
    @DisplayName("Should not update when comment title is null")
    void updateComment_ShouldNotUpdate_WhenCommentTitleIsNull() {
        // Given
        UpdateCommentDto nullUpdateDto = UpdateCommentDto.builder()
                .commentTitle(null)
                .build();
        
        String originalTitle = sampleComment.getCommentTitle();
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(sampleComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(sampleComment);

        // When
        commentService.updateComment(commentId, nullUpdateDto);

        // Then
        assertThat(sampleComment.getCommentTitle()).isEqualTo(originalTitle); // unchanged
        
        verify(commentRepository).findById(commentId);
        verify(commentRepository).save(sampleComment);
    }

    @Test
    @DisplayName("Should throw exception when updating non-existent comment")
    void updateComment_ShouldThrowException_WhenIdNotExists() {
        // Given
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.updateComment(commentId, updateDto))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage("Comment not found with id: " + commentId);
        
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should delete comment successfully when comment exists")
    void deleteComment_ShouldDeleteComment_WhenIdExists() {
        // Given
        when(commentRepository.findById(commentId)).thenReturn(Optional.of(sampleComment));

        // When
        commentService.deleteComment(commentId);

        // Then
        verify(commentRepository).findById(commentId);
        verify(commentRepository).delete(sampleComment);
    }

    @Test
    @DisplayName("Should throw exception when deleting non-existent comment")
    void deleteComment_ShouldThrowException_WhenIdNotExists() {
        // Given
        when(commentRepository.findById(commentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> commentService.deleteComment(commentId))
                .isInstanceOf(CommentNotFoundException.class)
                .hasMessage("Comment not found with id: " + commentId);
        
        verify(commentRepository).findById(commentId);
        verify(commentRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should return true when comment exists")
    void commentExists_ShouldReturnTrue_WhenIdExists() {
        // Given
        when(commentRepository.existsById(commentId)).thenReturn(true);

        // When
        boolean result = commentService.commentExists(commentId);

        // Then
        assertThat(result).isTrue();
        verify(commentRepository).existsById(commentId);
    }

    @Test
    @DisplayName("Should return false when comment does not exist")
    void commentExists_ShouldReturnFalse_WhenIdNotExists() {
        // Given
        when(commentRepository.existsById(commentId)).thenReturn(false);

        // When
        boolean result = commentService.commentExists(commentId);

        // Then
        assertThat(result).isFalse();
        verify(commentRepository).existsById(commentId);
    }

    @Test
    @DisplayName("Should return comments by IDs successfully")
    void getCommentsByIds_ShouldReturnComments_WhenIdsExist() {
        // Given
        UUID secondId = UUID.randomUUID();
        Comment secondComment = Comment.builder()
                .id(secondId)
                .commentTitle("Strong leadership qualities")
                .build();
        
        List<UUID> ids = Arrays.asList(commentId, secondId);
        List<Comment> comments = Arrays.asList(sampleComment, secondComment);
        
        when(commentRepository.findAllById(ids)).thenReturn(comments);

        // When
        List<CommentDto> result = commentService.getCommentsByIds(ids);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(commentId);
        assertThat(result.get(1).getId()).isEqualTo(secondId);
        assertThat(result.get(0).getCommentTitle()).isEqualTo("Excellent problem-solving skills");
        assertThat(result.get(1).getCommentTitle()).isEqualTo("Strong leadership qualities");
        
        verify(commentRepository).findAllById(ids);
    }

    @Test
    @DisplayName("Should return empty list when no comments found by IDs")
    void getCommentsByIds_ShouldReturnEmptyList_WhenNoIdsMatch() {
        // Given
        List<UUID> ids = Arrays.asList(UUID.randomUUID(), UUID.randomUUID());
        when(commentRepository.findAllById(ids)).thenReturn(Collections.emptyList());

        // When
        List<CommentDto> result = commentService.getCommentsByIds(ids);

        // Then
        assertThat(result).isEmpty();
        verify(commentRepository).findAllById(ids);
    }

    @Test
    @DisplayName("Should handle empty ID list gracefully")
    void getCommentsByIds_ShouldReturnEmptyList_WhenEmptyIdList() {
        // Given
        List<UUID> emptyIds = Collections.emptyList();
        when(commentRepository.findAllById(emptyIds)).thenReturn(Collections.emptyList());

        // When
        List<CommentDto> result = commentService.getCommentsByIds(emptyIds);

        // Then
        assertThat(result).isEmpty();
        verify(commentRepository).findAllById(emptyIds);
    }
}