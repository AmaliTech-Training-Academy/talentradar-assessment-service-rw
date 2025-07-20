package com.talentradar.assessment_service.dto.feedbackComment.response;

import com.talentradar.assessment_service.dto.comment.response.CommentDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackCommentDto {
    private UUID id;
    private UUID feedbackId;
    private CommentDto comment;
    private String feedbackCommentBody;
}