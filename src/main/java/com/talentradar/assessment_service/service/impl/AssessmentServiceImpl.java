package com.talentradar.assessment_service.service.impl;

import com.talentradar.assessment_service.dto.assessment.request.AssessmentRequestDTO;
import com.talentradar.assessment_service.dto.assessment.request.DimensionRatingDTO;
import com.talentradar.assessment_service.dto.assessment.response.AssessmentResponseDTO;
import com.talentradar.assessment_service.dto.assessment.response.PaginatedResponseDTO;
//import com.talentradar.assessment_service.event.producer.AssessmentEventProducer;
import com.talentradar.assessment_service.event.rabbit.producer.AssessmentEventProducer;
import com.talentradar.assessment_service.exception.BadRequestException;
import com.talentradar.assessment_service.exception.ResourceNotFoundException;
import com.talentradar.assessment_service.mapper.AssessmentMapper;
import com.talentradar.assessment_service.model.Assessment;
import com.talentradar.assessment_service.model.AssessmentDimension;
import com.talentradar.assessment_service.model.DimensionDefinition;
import com.talentradar.assessment_service.model.SubmissionStatus;
import com.talentradar.assessment_service.repository.AssessmentDimensionRepository;
import com.talentradar.assessment_service.repository.AssessmentRepository;
import com.talentradar.assessment_service.repository.DimensionDefinitionRepository;
import com.talentradar.assessment_service.repository.UserSnapshotRepository;
import com.talentradar.assessment_service.service.AssessmentService;
import com.talentradar.assessment_service.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentDimensionRepository dimensionRepository;
    private final DimensionDefinitionRepository dimensionDefinitionRepository;
    private final AssessmentMapper assessmentMapper;
    private final UserSnapshotRepository userSnapshotRepository;
    private final AssessmentEventProducer assessmentEventProducer;

    @Transactional
    @Override
    public AssessmentResponseDTO createAssessment(AssessmentRequestDTO requestDto, UUID userId) {
        log.info("Starting assessment creation for userId={}", userId);


        validateDimensionDefinitionIds(requestDto.getDimensions());

        reSubmissionValidation(userId);

        // Calculate weighted average score
        int averageScore = calculateWeightedAverageScore(requestDto.getDimensions());

        Assessment assessment = Assessment.builder()
                .userId(userId)
                .reflection(requestDto.getReflection())
                .submissionStatus(requestDto.getStatus())
                .averageScore(averageScore)
                .build();

        Assessment savedAssessment = assessmentRepository.save(assessment);
        log.info("Assessment saved with id={} and averageScore={}", savedAssessment.getId(), averageScore);

        List<AssessmentDimension> dimensions = requestDto.getDimensions().stream()
                .map(dim -> {
                    DimensionDefinition definition = getDimension(dim.getDimensionDefinitionId());
                    return AssessmentDimension.builder()
                            .assessment(savedAssessment)
                            .dimensionDefinition(definition)
                            .rating(dim.getRating())
                            .build();
                }).toList();

        dimensionRepository.saveAll(dimensions);
        log.info("Saved {} assessment dimensions for assessmentId={}", dimensions.size(), savedAssessment.getId());

        savedAssessment.setDimensions(dimensions);

        // Publish assessment event to Kafka for AI analysis
        try {
            assessmentEventProducer.publishAssessmentSubmitted(savedAssessment);
            log.info("Assessment event published successfully for assessmentId={}", savedAssessment.getId());
        } catch (Exception e) {
            log.error("Failed to publish assessment event for assessmentId={}: {}",
                    savedAssessment.getId(), e.getMessage(), e);
            // Don't fail the transaction if event publishing fails
        }

        return assessmentMapper.toResponseDto(savedAssessment);
    }

    private int calculateWeightedAverageScore(List<DimensionRatingDTO> dimensions) {
        log.debug("Calculating weighted average score for {} dimensions", dimensions.size());

        BigDecimal totalWeightedScore = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;

        for (DimensionRatingDTO dimension : dimensions) {
            DimensionDefinition definition = getDimension(dimension.getDimensionDefinitionId());

            BigDecimal rating = BigDecimal.valueOf(dimension.getRating());
            BigDecimal weight = definition.getWeight();

            BigDecimal weightedScore = rating.multiply(weight);
            totalWeightedScore = totalWeightedScore.add(weightedScore);
            totalWeight = totalWeight.add(weight);

            log.debug("Dimension: {}, Rating: {}, Weight: {}, Weighted Score: {}",
                    definition.getDimensionName(), rating, weight, weightedScore);
        }

        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            log.warn("Total weight is zero, returning 0 as average score");
            return 0;
        }

        // Calculate weighted average and round to nearest integer
        BigDecimal weightedAverage = totalWeightedScore.divide(totalWeight, 2, RoundingMode.HALF_UP);
        int averageScore = weightedAverage.setScale(0, RoundingMode.HALF_UP).intValue();

        log.debug("Total weighted score: {}, Total weight: {}, Average score: {}",
                totalWeightedScore, totalWeight, averageScore);

        return averageScore;
    }

    private DimensionDefinition getDimension(UUID id) {
        return dimensionDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DimensionDefinition with id " + id + " not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<AssessmentResponseDTO> getAllAssessmentsByUser(UUID userId, Pageable pageable) {
        log.info("Fetching assessments for userId={} with pagination={}", userId, pageable);

        Page<Assessment> page = assessmentRepository.findAllByUserIdWithDimensions(userId, pageable);
        log.info("Found {} assessments for userId={}", page.getTotalElements(), userId);

        return PaginationUtil.toPaginatedResponse(
                page.map(assessmentMapper::toResponseDto)
        );
    }

    private void validateDimensionDefinitionIds(List<DimensionRatingDTO> ratings) {
        List<UUID> ids = ratings.stream()
                .map(DimensionRatingDTO::getDimensionDefinitionId)
                .toList();

        List<UUID> existingIds = dimensionDefinitionRepository.findExistingIds(ids);
        log.debug("Validating dimension IDs. Provided={}, Existing={}", ids.size(), existingIds.size());

        if (existingIds.size() != ids.size()) {
            throw new BadRequestException("Some DimensionDefinition IDs are invalid");
        }
    }

    private void reSubmissionValidation(UUID userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        boolean recentlySubmitted = assessmentRepository
                .existsByUserIdAndSubmissionStatusAndCreatedAtAfter(userId, SubmissionStatus.SUBMITTED, thirtyDaysAgo);

        if (recentlySubmitted) {
            log.warn("Re-submission attempt within 30 days by userId={}", userId);
            throw new BadRequestException("User has already submitted an assessment within the last 30 days.");
        }
    }
}
