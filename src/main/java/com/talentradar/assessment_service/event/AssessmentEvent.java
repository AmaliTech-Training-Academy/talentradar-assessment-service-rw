package com.talentradar.assessment_service.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssessmentEvent {
    private AssessmentEventType eventType;
    private UUID assessmentId;
    private UUID userId;
    private String reflection;
    private Integer averageScore;
    private String submissionStatus;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String eventId;
    private String source;

    // User context for analysis
    private UserContext userContext;
}
