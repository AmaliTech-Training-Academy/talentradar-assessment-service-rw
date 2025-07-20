package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.comment.response.CommentDto;
import com.talentradar.assessment_service.dto.comment.request.CreateCommentDto;
import com.talentradar.assessment_service.dto.comment.request.CreateCommentDto;
import java.util.List;
import java.util.UUID;

public interface CommentService {
    
    List<CommentDto> getAllComments();
    
    CommentDto getCommentById(UUID id);
    
    CommentDto createComment(CreateCommentDto createDto);
    
    CommentDto updateComment(UUID id, CreateCommentDto updateDto);
    
    void deleteComment(UUID id);
    
    boolean commentExists(UUID id);
    
    List<CommentDto> getCommentsByIds(List<UUID> ids);
}