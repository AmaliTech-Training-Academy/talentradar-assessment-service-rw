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
public class FeedbackEvent {
    private FeedbackEventType eventType;
    private UUID feedbackId;
    private UUID managerId;
    private UUID developerId;
    private int feedbackVersion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    private String eventId;
    private String source;

    // User context for analysis - manager and developer info
    private UserContext managerContext;
    private UserContext developerContext;
}