package com.talentradar.assessment_service.service.impl;

import com.talentradar.assessment_service.dto.comment.response.CommentDto;
import com.talentradar.assessment_service.dto.comment.request.CreateCommentDto;
import com.talentradar.assessment_service.dto.comment.request.CreateCommentDto;
import com.talentradar.assessment_service.exception.CommentNotFoundException;
import com.talentradar.assessment_service.model.Comment;
import com.talentradar.assessment_service.repository.CommentRepository;
import com.talentradar.assessment_service.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {
    
    private final CommentRepository commentRepository;
    
    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllComments() {
        return commentRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public CommentDto getCommentById(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + id));
        return mapToDto(comment);
    }
    
    @Override
    public CommentDto createComment(CreateCommentDto createDto) {
        Comment comment = Comment.builder()
                .commentTitle(createDto.getCommentTitle())
                .build();
        
        Comment savedComment = commentRepository.save(comment);
        return mapToDto(savedComment);
    }
    
    @Override
    public CommentDto updateComment(UUID id, CreateCommentDto updateDto) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + id));
        
        if (updateDto.getCommentTitle() != null) {
            comment.setCommentTitle(updateDto.getCommentTitle());
        }
        
        Comment updatedComment = commentRepository.save(comment);
        return mapToDto(updatedComment);
    }
    
    @Override
    public void deleteComment(UUID id) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id: " + id));
        commentRepository.delete(comment);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean commentExists(UUID id) {
        return commentRepository.existsById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getCommentsByIds(List<UUID> ids) {
        return commentRepository.findAllById(ids)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    
    private CommentDto mapToDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .commentTitle(comment.getCommentTitle())
                .build();
    }
}