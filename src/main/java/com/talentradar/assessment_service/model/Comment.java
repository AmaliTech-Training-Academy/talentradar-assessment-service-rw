package com.talentradar.assessment_service.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "comment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    private UUID id;

    @Column(name = "comment_title", nullable = false)
    private String commentTitle;

    @Column(name = "comment_content", nullable = false)
    private String commentContent;
}

