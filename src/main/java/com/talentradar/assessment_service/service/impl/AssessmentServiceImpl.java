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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AssessmentServiceImpl implements AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final AssessmentDimensionRepository dimensionRepository;
    private final DimensionDefinitionRepository dimensionDefinitionRepository;
    private final AssessmentMapper assessmentMapper;
    private final UserSnapshotRepository userSnapshotRepository;

    @Transactional
    @Override
    public AssessmentResponseDTO createAssessment(AssessmentRequestDTO requestDto, UUID userId) {

        userSnapshotRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + userId + " not found"));

        validateDimensionDefinitionIds(requestDto.getDimensions());

        reSubmissionValidation(userId);

        Assessment assessment = Assessment.builder()
                .userId(userId)
                .reflection(requestDto.getReflection())
                .submissionStatus(requestDto.getStatus())
                .build();

        Assessment savedAssessment = assessmentRepository.save(assessment);

        List<AssessmentDimension> dimensions = requestDto.getDimensions().stream()
                .map(dim -> AssessmentDimension.builder()
                        .assessment(savedAssessment)
                        .dimensionDefinition(getDimension(dim.getDimensionDefinitionId()))
                        .rating(dim.getRating())
                        .build())
                .toList();

        dimensionRepository.saveAll(dimensions);

        savedAssessment.setDimensions(dimensions);
        return assessmentMapper.toResponseDto(savedAssessment);
    }

    private DimensionDefinition getDimension(UUID id) {
        return dimensionDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DimensionDefinition with id " + id + " not found"));
    }


    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<AssessmentResponseDTO> getAssessmentsByUser(UUID userId, Pageable pageable) {
        userSnapshotRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Page<Assessment> page = assessmentRepository.findAllByUserId(userId, pageable);

        return PaginationUtil.toPaginatedResponse(
                page.map(assessmentMapper::toResponseDto)
        );
    }

    private void validateDimensionDefinitionIds(List<DimensionRatingDTO> ratings) {
        List<UUID> ids = ratings.stream()
                .map(DimensionRatingDTO::getDimensionDefinitionId)
                .toList();

        List<UUID> existingIds = dimensionDefinitionRepository.findExistingIds(ids);

        if (existingIds.size() != ids.size()) {
            throw new BadRequestException("Some DimensionDefinition IDs are invalid");
        }
    }

    private void reSubmissionValidation(UUID userId) {
        //verify if re-submission is within 30 days
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        boolean recentlySubmitted = assessmentRepository
                .existsByUserIdAndSubmissionStatusAndCreatedAtAfter(userId, SubmissionStatus.SUBMITTED, thirtyDaysAgo);

        if (recentlySubmitted) {
            throw new BadRequestException("User has already submitted an assessment within the last 30 days.");
        }
    }

}

