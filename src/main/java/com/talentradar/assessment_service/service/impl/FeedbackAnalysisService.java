package com.talentradar.assessment_service.service.impl;

import com.talentradar.assessment_service.dto.analysis.FeedbackAnalysisDto;
import com.talentradar.assessment_service.dto.feedbackComment.response.FeedbackCommentDto;
import com.talentradar.assessment_service.dto.feedbackDimension.response.FeedbackDimensionDto;
import com.talentradar.assessment_service.model.Assessment;
import com.talentradar.assessment_service.model.AssessmentDimension;
import com.talentradar.assessment_service.model.Feedback;
import com.talentradar.assessment_service.model.SubmissionStatus;
import com.talentradar.assessment_service.repository.AssessmentRepository;
import com.talentradar.assessment_service.service.FeedbackCommentService;
import com.talentradar.assessment_service.service.FeedbackDimensionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FeedbackAnalysisService {
    
    private final AssessmentRepository assessmentRepository;
    private final FeedbackDimensionService feedbackDimensionService;
    private final FeedbackCommentService feedbackCommentService;
    
    /**
     * Creates a combined analysis DTO with user's self-assessment and manager feedback
     * @param feedback The submitted feedback
     * @return FeedbackAnalysisDto with combined data
     */
    public FeedbackAnalysisDto createAnalysisDto(Feedback feedback) {
        log.info("Creating analysis DTO for feedback: {} (Manager: {}, Developer: {})", 
                feedback.getId(), feedback.getManagerId(), feedback.getDeveloperId());
        
        // Get the latest submitted assessment for the developer
        Assessment latestAssessment = getLatestSubmittedAssessment(feedback.getDeveloperId());
        if (latestAssessment == null) {
            log.warn("No submitted assessment found for developer: {}", feedback.getDeveloperId());
            return createFeedbackOnlyAnalysis(feedback);
        }
        
        FeedbackAnalysisDto.SelfAssessmentData selfAssessment = buildSelfAssessmentData(latestAssessment);
        
        FeedbackAnalysisDto.ManagerFeedbackData managerFeedback = buildManagerFeedbackData(feedback);
        
        FeedbackAnalysisDto analysisDto = FeedbackAnalysisDto.builder()
                .userId(feedback.getDeveloperId().toString())
                .selfAssessment(selfAssessment)
                .managerFeedback(managerFeedback)
                .build();
        
        log.info("Successfully created analysis DTO for user: {}", feedback.getDeveloperId());
        return analysisDto;
    }
    
    private Assessment getLatestSubmittedAssessment(UUID developerId) {
        return assessmentRepository.findLatestSubmittedAssessmentByUserId(developerId)
                .orElse(null);
    }
    
    private FeedbackAnalysisDto.SelfAssessmentData buildSelfAssessmentData(Assessment assessment) {
        Map<String, Integer> scores = new HashMap<>();
        
        if (assessment.getDimensions() != null) {
            for (AssessmentDimension dimension : assessment.getDimensions()) {
                String dimensionName = dimension.getDimensionDefinition().getDimensionName()
                        .toLowerCase()
                        .replaceAll("\\s+", "")
                        .replaceAll("&", "");
                scores.put(dimensionName, dimension.getRating());
            }
        }
        
        return FeedbackAnalysisDto.SelfAssessmentData.builder()
                .scores(scores)
                .reflection(assessment.getReflection())
                .build();
    }
    
    private FeedbackAnalysisDto.ManagerFeedbackData buildManagerFeedbackData(Feedback feedback) {

        List<FeedbackDimensionDto> dimensions = feedbackDimensionService
                .getFeedbackDimensionsByFeedbackId(feedback.getId());
        
        Map<String, Integer> scores = new HashMap<>();
        for (FeedbackDimensionDto dimension : dimensions) {
            String dimensionName = dimension.getDimensionDefinition().getDimensionName()
                    .toLowerCase()
                    .replaceAll("\\s+", "")
                    .replaceAll("&", "");
            scores.put(dimensionName, dimension.getRating());
        }
        
        // Get feedback comments and combine them
        List<FeedbackCommentDto> comments = feedbackCommentService
                .getFeedbackCommentsByFeedbackId(feedback.getId());
        
        String combinedReflection = comments.stream()
                .map(comment -> comment.getComment().getCommentTitle() + ": " + comment.getFeedbackCommentBody())
                .collect(Collectors.joining(" | "));
        
        return FeedbackAnalysisDto.ManagerFeedbackData.builder()
                .scores(scores)
                .reflection(combinedReflection)
                .build();
    }
    
    private FeedbackAnalysisDto createFeedbackOnlyAnalysis(Feedback feedback) {
        // Create analysis with only manager feedback data
        FeedbackAnalysisDto.ManagerFeedbackData managerFeedback = buildManagerFeedbackData(feedback);
        
        return FeedbackAnalysisDto.builder()
                .userId(feedback.getDeveloperId().toString())
                .selfAssessment(null) // No self-assessment available
                .managerFeedback(managerFeedback)
                .build();
    }
}