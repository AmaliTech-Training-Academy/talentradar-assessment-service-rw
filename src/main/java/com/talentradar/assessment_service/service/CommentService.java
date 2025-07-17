package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.comment.CommentDto;
import com.talentradar.assessment_service.dto.comment.CreateCommentDto;
import com.talentradar.assessment_service.dto.comment.UpdateCommentDto;
import java.util.List;
import java.util.UUID;

public interface CommentService {
    
    /**
     * Get all comments
     */
    List<CommentDto> getAllComments();
    
    /**
     * Get comment by ID
     */
    CommentDto getCommentById(UUID id);
    
    /**
     * Create new comment
     */
    CommentDto createComment(CreateCommentDto createDto);
    
    /**
     * Update existing comment
     */
    CommentDto updateComment(UUID id, UpdateCommentDto updateDto);
    
    /**
     * Delete comment
     */
    void deleteComment(UUID id);
    
    /**
     * Check if comment exists
     */
    boolean commentExists(UUID id);
    
    /**
     * Get multiple comments by IDs (for batch operations)
     */
    List<CommentDto> getCommentsByIds(List<UUID> ids);
}