package com.talentradar.assessment_service.service.impl;

import com.talentradar.assessment_service.dto.assessment.request.AssessmentRequestDTO;
import com.talentradar.assessment_service.dto.assessment.request.DimensionRatingDTO;
import com.talentradar.assessment_service.dto.assessment.response.AssessmentResponseDTO;
import com.talentradar.assessment_service.dto.assessment.response.PaginatedResponseDTO;
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

    @Transactional
    @Override
    public AssessmentResponseDTO createAssessment(AssessmentRequestDTO requestDto, UUID userId) {
        log.info("Starting assessment creation for userId={}", userId);

        userSnapshotRepository.findById(userId)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("User with id: " + userId + " not found");
                });

        validateDimensionDefinitionIds(requestDto.getDimensions());

        reSubmissionValidation(userId);

        Assessment assessment = Assessment.builder()
                .userId(userId)
                .reflection(requestDto.getReflection())
                .submissionStatus(requestDto.getStatus())
                .build();

        Assessment savedAssessment = assessmentRepository.save(assessment);
        log.info("Assessment saved with id={}", savedAssessment.getId());

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
        return assessmentMapper.toResponseDto(savedAssessment);
    }

    private DimensionDefinition getDimension(UUID id) {
        return dimensionDefinitionRepository.findById(id)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("DimensionDefinition with id " + id + " not found");
                });
    }

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<AssessmentResponseDTO> getAssessmentsByUser(UUID userId, Pageable pageable) {
        log.info("Fetching assessments for userId={} with pagination={}", userId, pageable);

        userSnapshotRepository.findById(userId)
                .orElseThrow(() -> {
                    return new ResourceNotFoundException("User not found with ID: " + userId);
                });

        Page<Assessment> page = assessmentRepository.findAllByUserId(userId, pageable);
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
