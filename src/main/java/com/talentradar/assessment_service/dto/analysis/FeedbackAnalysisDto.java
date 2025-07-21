package com.talentradar.assessment_service.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedbackAnalysisDto {
    
    private String userId;
    private SelfAssessmentData selfAssessment;
    private ManagerFeedbackData managerFeedback;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SelfAssessmentData {
        private Map<String, Integer> scores; // dimension name -> rating
        private String reflection;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ManagerFeedbackData {
        private Map<String, Integer> scores; // dimension name -> rating
        private String reflection; // combined feedback comments
    }
}