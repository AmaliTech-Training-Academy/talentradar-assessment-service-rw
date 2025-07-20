package com.talentradar.assessment_service.service.impl;

import com.talentradar.assessment_service.dto.assessment.response.PaginatedResponseDTO;
import com.talentradar.assessment_service.dto.feedback.request.CreateCompleteFeedbackDto;
import com.talentradar.assessment_service.dto.feedback.request.FeedbackSearchCriteria;
import com.talentradar.assessment_service.dto.feedback.response.FeedbackDto;
import com.talentradar.assessment_service.dto.feedbackComment.request.CreateFeedbackCommentDto;
import com.talentradar.assessment_service.dto.feedbackComment.response.FeedbackCommentDto;
import com.talentradar.assessment_service.dto.feedbackDimension.request.CreateFeedbackDimensionDto;
import com.talentradar.assessment_service.dto.feedbackDimension.response.FeedbackDimensionDto;
import com.talentradar.assessment_service.event.producer.FeedbackEventProducer;
import com.talentradar.assessment_service.exception.FeedbackNotFoundException;
import com.talentradar.assessment_service.model.Feedback;
import com.talentradar.assessment_service.repository.FeedbackRepository;
import com.talentradar.assessment_service.repository.specification.FeedbackSpecification;
import com.talentradar.assessment_service.service.FeedbackCommentService;
import com.talentradar.assessment_service.service.FeedbackDimensionService;
import com.talentradar.assessment_service.service.FeedbackService;
import com.talentradar.assessment_service.util.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class FeedbackServiceImpl implements FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackDimensionService feedbackDimensionService;
    private final FeedbackCommentService feedbackCommentService;
    private final FeedbackEventProducer feedbackEventProducer;

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackDto> getAllFeedback() {
        return feedbackRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackDto getFeedbackById(UUID id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found with id: " + id));
        return mapToDto(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackDto> getFeedbackByManagerId(UUID managerId) {
        return feedbackRepository.findByManagerId(managerId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackDto> getFeedbackByDeveloperId(UUID developerId) {
        return feedbackRepository.findByDeveloperId(developerId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackDto> getFeedbackByManagerAndDeveloper(UUID managerId, UUID developerId) {
        return feedbackRepository.findByManagerIdAndDeveloperIdOrderByFeedbackVersionDesc(managerId, developerId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackDto getLatestFeedbackVersion(UUID managerId, UUID developerId) {
        Feedback feedback = feedbackRepository.findTopByManagerIdAndDeveloperIdOrderByFeedbackVersionDesc(managerId, developerId)
                .orElseThrow(() -> new FeedbackNotFoundException("No feedback found for manager: " + managerId + " and developer: " + developerId));
        return mapToDtoWithDetails(feedback);
    }

    @Override
    public void deleteFeedback(UUID id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found with id: " + id));

        feedbackRepository.delete(feedback);

        // Publish feedback deleted event
        try {
            feedbackEventProducer.publishFeedbackDeleted(feedback);
            log.info("Feedback deleted event published successfully for feedbackId={}", id);
        } catch (Exception e) {
            log.error("Failed to publish feedback deleted event for feedbackId={}: {}",
                    id, e.getMessage(), e);
            // Don't fail the transaction if event publishing fails
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean feedbackExists(UUID id) {
        return feedbackRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeedbackDto> getFeedbackByIds(List<UUID> ids) {
        return feedbackRepository.findAllById(ids)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public FeedbackDto createNewFeedbackVersion(UUID managerId, UUID developerId) {
        int latestVersion = feedbackRepository.findTopByManagerIdAndDeveloperIdOrderByFeedbackVersionDesc(managerId, developerId)
                .map(feedback -> feedback.getFeedbackVersion() + 1)
                .orElse(1);

        Feedback feedback = Feedback.builder()
                .managerId(managerId)
                .developerId(developerId)
                .feedbackVersion(latestVersion)
                .build();

        Feedback savedFeedback = feedbackRepository.save(feedback);

        // Publish feedback version created event
        try {
            feedbackEventProducer.publishFeedbackVersionCreated(savedFeedback);
            log.info("Feedback version created event published successfully for feedbackId={}",
                    savedFeedback.getId());
        } catch (Exception e) {
            log.error("Failed to publish feedback version created event for feedbackId={}: {}",
                    savedFeedback.getId(), e.getMessage(), e);
            // Don't fail the transaction if event publishing fails
        }

        return mapToDto(savedFeedback);
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackDto getFeedbackWithDetails(UUID id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found with id: " + id));
        return mapToDtoWithDetails(feedback);
    }

    @Override
    public FeedbackDto createCompleteFeedback(CreateCompleteFeedbackDto createDto) {
        log.info("Creating complete feedback for manager: {} and developer: {}",
                createDto.getManagerId(), createDto.getDeveloperId());

        // Calculate the next version number based on existing feedback between this manager and developer
        int nextVersion = feedbackRepository.findTopByManagerIdAndDeveloperIdOrderByFeedbackVersionDesc(
                        createDto.getManagerId(), createDto.getDeveloperId())
                .map(existingFeedback -> existingFeedback.getFeedbackVersion() + 1)
                .orElse(1); // First feedback starts at version 1

        log.info("Calculated feedback version: {} for manager: {} and developer: {}",
                nextVersion, createDto.getManagerId(), createDto.getDeveloperId());

        Feedback feedback = Feedback.builder()
                .managerId(createDto.getManagerId())
                .developerId(createDto.getDeveloperId())
                .feedbackVersion(nextVersion)
                .build();

        Feedback savedFeedback = feedbackRepository.save(feedback);
        log.info("Created feedback with ID: {}", savedFeedback.getId());

        List<FeedbackDimensionDto> dimensionDtos = createDto.getDimensions().stream()
                .map(dimensionRequest -> {
                    CreateFeedbackDimensionDto dimensionDto = CreateFeedbackDimensionDto.builder()
                            .feedbackId(savedFeedback.getId())
                            .dimensionDefinitionId(dimensionRequest.getDimensionDefinitionId())
                            .rating(dimensionRequest.getRating())
                            .comment(dimensionRequest.getComment())
                            .build();
                    return feedbackDimensionService.createFeedbackDimension(dimensionDto);
                })
                .collect(Collectors.toList());

        log.info("Created {} feedback dimensions", dimensionDtos.size());

        List<FeedbackCommentDto> commentDtos = Collections.emptyList();
        if (createDto.getFeedbackComments() != null && !createDto.getFeedbackComments().isEmpty()) {
            commentDtos = createDto.getFeedbackComments().stream()
                    .map(commentRequest -> {
                        CreateFeedbackCommentDto commentDto = CreateFeedbackCommentDto.builder()
                                .feedbackId(savedFeedback.getId())
                                .commentId(commentRequest.getCommentId())
                                .feedbackCommentBody(commentRequest.getFeedbackCommentBody())
                                .build();
                        return feedbackCommentService.createFeedbackComment(commentDto);
                    })
                    .collect(Collectors.toList());
        }

        log.info("Created {} feedback comments", commentDtos.size());

        // Publish feedback created event
        try {
            feedbackEventProducer.publishFeedbackCreated(savedFeedback);
            log.info("Feedback created event published successfully for feedbackId={}",
                    savedFeedback.getId());
        } catch (Exception e) {
            log.error("Failed to publish feedback created event for feedbackId={}: {}",
                    savedFeedback.getId(), e.getMessage(), e);
            // Don't fail the transaction if event publishing fails
        }

        return FeedbackDto.builder()
                .id(savedFeedback.getId())
                .managerId(savedFeedback.getManagerId())
                .developerId(savedFeedback.getDeveloperId())
                .feedbackVersion(savedFeedback.getFeedbackVersion())
                .dimensions(dimensionDtos)
                .feedbackComments(commentDtos)
                .createdAt(savedFeedback.getCreatedAt())
                .updatedAt(savedFeedback.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FeedbackDto getCompleteFeedback(UUID id) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found with id: " + id));

        List<FeedbackDimensionDto> dimensions = feedbackDimensionService
                .getFeedbackDimensionsByFeedbackId(feedback.getId());

        List<FeedbackCommentDto> comments = feedbackCommentService
                .getFeedbackCommentsByFeedbackId(feedback.getId());

        return FeedbackDto.builder()
                .id(feedback.getId())
                .managerId(feedback.getManagerId())
                .developerId(feedback.getDeveloperId())
                .feedbackVersion(feedback.getFeedbackVersion())
                .dimensions(dimensions)
                .feedbackComments(comments)
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .build();
    }

    @Override
    public FeedbackDto updateCompleteFeedback(UUID id, CreateCompleteFeedbackDto updateDto) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new FeedbackNotFoundException("Feedback not found with id: " + id));

        feedback.setManagerId(updateDto.getManagerId());
        feedback.setDeveloperId(updateDto.getDeveloperId());
        feedback.setFeedbackVersion(updateDto.getFeedbackVersion());

        Feedback updatedFeedback = feedbackRepository.save(feedback);

        // Publish feedback updated event
        try {
            feedbackEventProducer.publishFeedbackUpdated(updatedFeedback);
            log.info("Feedback updated event published successfully for feedbackId={}", id);
        } catch (Exception e) {
            log.error("Failed to publish feedback updated event for feedbackId={}: {}",
                    id, e.getMessage(), e);
            // Don't fail the transaction if event publishing fails
        }

        return getCompleteFeedback(updatedFeedback.getId());
    }

    private FeedbackDto mapToDto(Feedback feedback) {
        return FeedbackDto.builder()
                .id(feedback.getId())
                .managerId(feedback.getManagerId())
                .developerId(feedback.getDeveloperId())
                .feedbackVersion(feedback.getFeedbackVersion())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .dimensions(Collections.emptyList())
                .feedbackComments(Collections.emptyList())
                .build();
    }

    private FeedbackDto mapToDtoWithDetails(Feedback feedback) {
        List<FeedbackDimensionDto> dimensions = feedbackDimensionService
                .getFeedbackDimensionsByFeedbackId(feedback.getId());

        List<FeedbackCommentDto> comments = feedbackCommentService
                .getFeedbackCommentsByFeedbackId(feedback.getId());

        return FeedbackDto.builder()
                .id(feedback.getId())
                .managerId(feedback.getManagerId())
                .developerId(feedback.getDeveloperId())
                .feedbackVersion(feedback.getFeedbackVersion())
                .createdAt(feedback.getCreatedAt())
                .updatedAt(feedback.getUpdatedAt())
                .dimensions(dimensions)
                .feedbackComments(comments)
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponseDTO<FeedbackDto> searchFeedbacks(FeedbackSearchCriteria criteria, Pageable pageable) {
        log.info("Searching feedbacks with criteria: managerId={}, developerId={}, version={}, createdAfter={}, createdBefore={}",
                criteria.getManagerId(), criteria.getDeveloperId(), criteria.getFeedbackVersion(),
                criteria.getCreatedAfter(), criteria.getCreatedBefore());

        Specification<Feedback> specification = FeedbackSpecification.createSpecification(criteria);
        Page<Feedback> feedbackPage = feedbackRepository.findAll(specification, pageable);

        log.info("Found {} feedbacks matching criteria", feedbackPage.getTotalElements());

        return PaginationUtil.toPaginatedResponse(
                feedbackPage.map(this::mapToDto)
        );
    }
}