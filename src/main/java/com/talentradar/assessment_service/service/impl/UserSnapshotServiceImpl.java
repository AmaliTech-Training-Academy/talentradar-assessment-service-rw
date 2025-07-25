package com.talentradar.assessment_service.service.impl;

import com.talentradar.assessment_service.dto.userSnapshot.response.UserSnapshotDto;
import com.talentradar.assessment_service.exception.ResourceNotFoundException;
import com.talentradar.assessment_service.exception.BadRequestException;
import com.talentradar.assessment_service.model.SubmissionStatus;
import com.talentradar.assessment_service.model.UserRole;
import com.talentradar.assessment_service.model.UserSnapshot;
import com.talentradar.assessment_service.repository.AssessmentRepository;
import com.talentradar.assessment_service.repository.UserSnapshotRepository;
import com.talentradar.assessment_service.service.UserSnapshotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserSnapshotServiceImpl implements UserSnapshotService {

    private final UserSnapshotRepository userSnapshotRepository;
    private final AssessmentRepository assessmentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<UserSnapshotDto> getDevelopersByManagerId(UUID managerId) {
        log.info("Retrieving developers for manager: {}", managerId);
        
        // Validate that the manager exists and has MANAGER role
        UserSnapshot manager = userSnapshotRepository.findByUserId(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + managerId));
                
        if (manager.getRole() != UserRole.MANAGER) {
            throw new BadRequestException("User with id " + managerId + " is not a manager");
        }
        
        List<UserSnapshot> developers = userSnapshotRepository.findByManagerIdAndRole(managerId, UserRole.DEVELOPER);
        log.info("Found {} developers for manager: {}", developers.size(), managerId);

        List<UserSnapshot> developersWithSubmittedAssessments = developers.stream()
                .filter(developer -> assessmentRepository.existsByUserIdAndSubmissionStatus(
                        developer.getUserId(), SubmissionStatus.SUBMITTED))
                .toList();


        return developersWithSubmittedAssessments.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserSnapshotDto assignDeveloperToManager(UUID developerId, UUID managerId) {
        log.info("Assigning developer {} to manager {}", developerId, managerId);
        
        // Validate developer exists and has DEVELOPER role
        UserSnapshot developer = userSnapshotRepository.findByUserId(developerId)
                .orElseThrow(() -> new ResourceNotFoundException("Developer not found with id: " + developerId));
                
        if (developer.getRole() != UserRole.DEVELOPER) {
            throw new BadRequestException("User with id " + developerId + " is not a developer");
        }
        
        // Validate manager exists and has MANAGER role
        UserSnapshot manager = userSnapshotRepository.findByUserId(managerId)
                .orElseThrow(() -> new ResourceNotFoundException("Manager not found with id: " + managerId));
                
        if (manager.getRole() != UserRole.MANAGER) {
            throw new BadRequestException("User with id " + managerId + " is not a manager");
        }
        
        // Check if developer is already assigned to this manager
        if (developer.getManagerId() != null && developer.getManagerId().equals(managerId)) {
            log.warn("Developer {} is already assigned to manager {}", developerId, managerId);
            return mapToDto(developer);
        }
        
        // Assign developer to manager
        developer.setManagerId(managerId);
        UserSnapshot savedDeveloper = userSnapshotRepository.save(developer);
        
        log.info("Successfully assigned developer {} to manager {}", developerId, managerId);
        return mapToDto(savedDeveloper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserSnapshotDto> getUnassignedDevelopers() {
        log.info("Retrieving unassigned developers");
        
        List<UserSnapshot> unassignedDevelopers = userSnapshotRepository
                .findByManagerIdIsNullAndRole(UserRole.DEVELOPER);
        
        log.info("Found {} unassigned developers", unassignedDevelopers.size());
        
        return unassignedDevelopers.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserSnapshotDto getUserById(UUID userId) {
        log.info("Retrieving user: {}", userId);
        
        UserSnapshot user = userSnapshotRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
                
        return mapToDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean userExists(UUID userId) {
        return userSnapshotRepository.findByUserId(userId).isPresent();
    }

    private UserSnapshotDto mapToDto(UserSnapshot userSnapshot) {
        return UserSnapshotDto.builder()
                .id(userSnapshot.getId())
                .userId(userSnapshot.getUserId())
                .managerId(userSnapshot.getManagerId())
                .fullName(userSnapshot.getFullName())
                .username(userSnapshot.getUsername())
                .email(userSnapshot.getEmail())
                .role(userSnapshot.getRole())
                .build();
    }
}