package com.talentradar.assessment_service.dto.comment.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateCommentDto {
    @NotBlank(message = "Comment title is required")
    private String commentTitle;
}
