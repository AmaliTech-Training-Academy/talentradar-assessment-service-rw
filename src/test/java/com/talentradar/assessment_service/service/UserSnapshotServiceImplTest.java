package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.userSnapshot.response.UserSnapshotDto;
import com.talentradar.assessment_service.exception.BadRequestException;
import com.talentradar.assessment_service.exception.ResourceNotFoundException;
import com.talentradar.assessment_service.model.SubmissionStatus;
import com.talentradar.assessment_service.model.UserRole;
import com.talentradar.assessment_service.model.UserSnapshot;
import com.talentradar.assessment_service.repository.AssessmentRepository;
import com.talentradar.assessment_service.repository.UserSnapshotRepository;
import com.talentradar.assessment_service.service.impl.UserSnapshotServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserSnapshotService Tests")
class UserSnapshotServiceImplTest {

    @Mock
    private UserSnapshotRepository userSnapshotRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    @InjectMocks
    private UserSnapshotServiceImpl userSnapshotService;

    private UUID managerId;
    private UUID developerId1;
    private UUID developerId2;
    private UUID developerId3;
    private UserSnapshot managerSnapshot;
    private UserSnapshot developer1Snapshot;
    private UserSnapshot developer2Snapshot;
    private UserSnapshot developer3Snapshot;

    @BeforeEach
    void setUp() {
        managerId = UUID.randomUUID();
        developerId1 = UUID.randomUUID();
        developerId2 = UUID.randomUUID();
        developerId3 = UUID.randomUUID();

        managerSnapshot = UserSnapshot.builder()
                .id(UUID.randomUUID())
                .userId(managerId)
                .fullName("John Manager")
                .username("john.manager")
                .email("john.manager@example.com")
                .role(UserRole.MANAGER)
                .build();

        developer1Snapshot = UserSnapshot.builder()
                .id(UUID.randomUUID())
                .userId(developerId1)
                .managerId(managerId)
                .fullName("Alice Developer")
                .username("alice.developer")
                .email("alice.developer@example.com")
                .role(UserRole.DEVELOPER)
                .build();

        developer2Snapshot = UserSnapshot.builder()
                .id(UUID.randomUUID())
                .userId(developerId2)
                .managerId(managerId)
                .fullName("Bob Developer")
                .username("bob.developer")
                .email("bob.developer@example.com")
                .role(UserRole.DEVELOPER)
                .build();

        developer3Snapshot = UserSnapshot.builder()
                .id(UUID.randomUUID())
                .userId(developerId3)
                .managerId(null) // Unassigned
                .fullName("Charlie Developer")
                .username("charlie.developer")
                .email("charlie.developer@example.com")
                .role(UserRole.DEVELOPER)
                .build();
    }

    @Test
    @DisplayName("Should return developers with submitted assessments for valid manager")
    void getDevelopersByManagerId_ShouldReturnDevelopersWithSubmittedAssessments_WhenValidManager() {
        // Given
        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.of(managerSnapshot));
        when(userSnapshotRepository.findByManagerIdAndRole(managerId, UserRole.DEVELOPER))
                .thenReturn(Arrays.asList(developer1Snapshot, developer2Snapshot));
        
        // Mock that both developers have submitted assessments
        when(assessmentRepository.existsByUserIdAndSubmissionStatus(developerId1, SubmissionStatus.SUBMITTED))
                .thenReturn(true);
        when(assessmentRepository.existsByUserIdAndSubmissionStatus(developerId2, SubmissionStatus.SUBMITTED))
                .thenReturn(true);

        // When
        List<UserSnapshotDto> result = userSnapshotService.getDevelopersByManagerId(managerId);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getUserId()).isEqualTo(developerId1);
        assertThat(result.get(0).getFullName()).isEqualTo("Alice Developer");
        assertThat(result.get(0).getManagerId()).isEqualTo(managerId);
        assertThat(result.get(1).getUserId()).isEqualTo(developerId2);
        assertThat(result.get(1).getFullName()).isEqualTo("Bob Developer");
        assertThat(result.get(1).getManagerId()).isEqualTo(managerId);

        verify(userSnapshotRepository).findByUserId(managerId);
        verify(userSnapshotRepository).findByManagerIdAndRole(managerId, UserRole.DEVELOPER);
        verify(assessmentRepository).existsByUserIdAndSubmissionStatus(developerId1, SubmissionStatus.SUBMITTED);
        verify(assessmentRepository).existsByUserIdAndSubmissionStatus(developerId2, SubmissionStatus.SUBMITTED);
    }

    @Test
    @DisplayName("Should filter out developers without submitted assessments")
    void getDevelopersByManagerId_ShouldFilterOutDevelopersWithoutSubmittedAssessments() {
        // Given
        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.of(managerSnapshot));
        when(userSnapshotRepository.findByManagerIdAndRole(managerId, UserRole.DEVELOPER))
                .thenReturn(Arrays.asList(developer1Snapshot, developer2Snapshot));
        
        // Mock that only developer1 has submitted assessment
        when(assessmentRepository.existsByUserIdAndSubmissionStatus(developerId1, SubmissionStatus.SUBMITTED))
                .thenReturn(true);
        when(assessmentRepository.existsByUserIdAndSubmissionStatus(developerId2, SubmissionStatus.SUBMITTED))
                .thenReturn(false);

        // When
        List<UserSnapshotDto> result = userSnapshotService.getDevelopersByManagerId(managerId);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(developerId1);
        assertThat(result.get(0).getFullName()).isEqualTo("Alice Developer");

        verify(userSnapshotRepository).findByUserId(managerId);
        verify(userSnapshotRepository).findByManagerIdAndRole(managerId, UserRole.DEVELOPER);
        verify(assessmentRepository).existsByUserIdAndSubmissionStatus(developerId1, SubmissionStatus.SUBMITTED);
        verify(assessmentRepository).existsByUserIdAndSubmissionStatus(developerId2, SubmissionStatus.SUBMITTED);
    }

    @Test
    @DisplayName("Should throw exception when manager not found")
    void getDevelopersByManagerId_ShouldThrowException_WhenManagerNotFound() {
        // Given
        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userSnapshotService.getDevelopersByManagerId(managerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Manager not found with id: " + managerId);

        verify(userSnapshotRepository).findByUserId(managerId);
        verifyNoMoreInteractions(userSnapshotRepository, assessmentRepository);
    }

    @Test
    @DisplayName("Should throw exception when user is not a manager")
    void getDevelopersByManagerId_ShouldThrowException_WhenUserIsNotManager() {
        // Given
        UserSnapshot developerPretendingToBeManager = UserSnapshot.builder()
                .userId(managerId)
                .role(UserRole.DEVELOPER)
                .build();
        
        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.of(developerPretendingToBeManager));

        // When & Then
        assertThatThrownBy(() -> userSnapshotService.getDevelopersByManagerId(managerId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User with id " + managerId + " is not a manager");

        verify(userSnapshotRepository).findByUserId(managerId);
        verifyNoMoreInteractions(userSnapshotRepository, assessmentRepository);
    }

    @Test
    @DisplayName("Should return empty list when manager has no developers with submitted assessments")
    void getDevelopersByManagerId_ShouldReturnEmptyList_WhenNoDevelopersWithSubmittedAssessments() {
        // Given
        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.of(managerSnapshot));
        when(userSnapshotRepository.findByManagerIdAndRole(managerId, UserRole.DEVELOPER))
                .thenReturn(Arrays.asList(developer1Snapshot));
        
        // Mock that developer has no submitted assessment
        when(assessmentRepository.existsByUserIdAndSubmissionStatus(developerId1, SubmissionStatus.SUBMITTED))
                .thenReturn(false);

        // When
        List<UserSnapshotDto> result = userSnapshotService.getDevelopersByManagerId(managerId);

        // Then
        assertThat(result).isEmpty();

        verify(userSnapshotRepository).findByUserId(managerId);
        verify(userSnapshotRepository).findByManagerIdAndRole(managerId, UserRole.DEVELOPER);
        verify(assessmentRepository).existsByUserIdAndSubmissionStatus(developerId1, SubmissionStatus.SUBMITTED);
    }

    @Test
    @DisplayName("Should assign developer to manager successfully")
    void assignDeveloperToManager_ShouldAssignSuccessfully_WhenValidDeveloperAndManager() {
        // Given
        when(userSnapshotRepository.findByUserId(developerId3)).thenReturn(Optional.of(developer3Snapshot));
        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.of(managerSnapshot));
        when(userSnapshotRepository.save(any(UserSnapshot.class))).thenReturn(developer3Snapshot);

        // When
        UserSnapshotDto result = userSnapshotService.assignDeveloperToManager(developerId3, managerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(developerId3);
        assertThat(result.getManagerId()).isEqualTo(managerId);

        verify(userSnapshotRepository).findByUserId(developerId3);
        verify(userSnapshotRepository).findByUserId(managerId);
        verify(userSnapshotRepository).save(developer3Snapshot);
        
        // Verify the developer's managerId was set
        assertThat(developer3Snapshot.getManagerId()).isEqualTo(managerId);
    }

    @Test
    @DisplayName("Should return existing assignment when developer already assigned to manager")
    void assignDeveloperToManager_ShouldReturnExisting_WhenDeveloperAlreadyAssigned() {
        // Given
        when(userSnapshotRepository.findByUserId(developerId1)).thenReturn(Optional.of(developer1Snapshot));
        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.of(managerSnapshot));

        // When
        UserSnapshotDto result = userSnapshotService.assignDeveloperToManager(developerId1, managerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(developerId1);
        assertThat(result.getManagerId()).isEqualTo(managerId);

        verify(userSnapshotRepository).findByUserId(developerId1);
        verify(userSnapshotRepository).findByUserId(managerId);
        verify(userSnapshotRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when developer not found for assignment")
    void assignDeveloperToManager_ShouldThrowException_WhenDeveloperNotFound() {
        // Given
        when(userSnapshotRepository.findByUserId(developerId1)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userSnapshotService.assignDeveloperToManager(developerId1, managerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Developer not found with id: " + developerId1);

        verify(userSnapshotRepository).findByUserId(developerId1);
        verifyNoMoreInteractions(userSnapshotRepository);
    }

    @Test
    @DisplayName("Should throw exception when trying to assign non-developer to manager")
    void assignDeveloperToManager_ShouldThrowException_WhenUserIsNotDeveloper() {
        // Given
        UserSnapshot managerPretendingToBeDeveloper = UserSnapshot.builder()
                .userId(developerId1)
                .role(UserRole.MANAGER)
                .build();
        
        when(userSnapshotRepository.findByUserId(developerId1)).thenReturn(Optional.of(managerPretendingToBeDeveloper));

        // When & Then
        assertThatThrownBy(() -> userSnapshotService.assignDeveloperToManager(developerId1, managerId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User with id " + developerId1 + " is not a developer");

        verify(userSnapshotRepository).findByUserId(developerId1);
        verifyNoMoreInteractions(userSnapshotRepository);
    }

    @Test
    @DisplayName("Should throw exception when manager not found for assignment")
    void assignDeveloperToManager_ShouldThrowException_WhenManagerNotFoundForAssignment() {
        // Given
        when(userSnapshotRepository.findByUserId(developerId3)).thenReturn(Optional.of(developer3Snapshot));
        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userSnapshotService.assignDeveloperToManager(developerId3, managerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Manager not found with id: " + managerId);

        verify(userSnapshotRepository).findByUserId(developerId3);
        verify(userSnapshotRepository).findByUserId(managerId);
        verifyNoMoreInteractions(userSnapshotRepository);
    }

    @Test
    @DisplayName("Should throw exception when trying to assign developer to non-manager")
    void assignDeveloperToManager_ShouldThrowException_WhenTargetUserIsNotManager() {
        // Given
        UserSnapshot developerPretendingToBeManager = UserSnapshot.builder()
                .userId(managerId)
                .role(UserRole.DEVELOPER)
                .build();
        
        when(userSnapshotRepository.findByUserId(developerId3)).thenReturn(Optional.of(developer3Snapshot));
        when(userSnapshotRepository.findByUserId(managerId)).thenReturn(Optional.of(developerPretendingToBeManager));

        // When & Then
        assertThatThrownBy(() -> userSnapshotService.assignDeveloperToManager(developerId3, managerId))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("User with id " + managerId + " is not a manager");

        verify(userSnapshotRepository).findByUserId(developerId3);
        verify(userSnapshotRepository).findByUserId(managerId);
        verifyNoMoreInteractions(userSnapshotRepository);
    }

    @Test
    @DisplayName("Should return unassigned developers successfully")
    void getUnassignedDevelopers_ShouldReturnUnassignedDevelopers_WhenTheyExist() {
        // Given
        when(userSnapshotRepository.findByManagerIdIsNullAndRole(UserRole.DEVELOPER))
                .thenReturn(Arrays.asList(developer3Snapshot));

        // When
        List<UserSnapshotDto> result = userSnapshotService.getUnassignedDevelopers();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(developerId3);
        assertThat(result.get(0).getFullName()).isEqualTo("Charlie Developer");
        assertThat(result.get(0).getManagerId()).isNull();

        verify(userSnapshotRepository).findByManagerIdIsNullAndRole(UserRole.DEVELOPER);
    }

    @Test
    @DisplayName("Should return empty list when no unassigned developers")
    void getUnassignedDevelopers_ShouldReturnEmptyList_WhenNoUnassignedDevelopers() {
        // Given
        when(userSnapshotRepository.findByManagerIdIsNullAndRole(UserRole.DEVELOPER))
                .thenReturn(Collections.emptyList());

        // When
        List<UserSnapshotDto> result = userSnapshotService.getUnassignedDevelopers();

        // Then
        assertThat(result).isEmpty();

        verify(userSnapshotRepository).findByManagerIdIsNullAndRole(UserRole.DEVELOPER);
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void getUserById_ShouldReturnUser_WhenUserExists() {
        // Given
        when(userSnapshotRepository.findByUserId(developerId1)).thenReturn(Optional.of(developer1Snapshot));

        // When
        UserSnapshotDto result = userSnapshotService.getUserById(developerId1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(developerId1);
        assertThat(result.getFullName()).isEqualTo("Alice Developer");
        assertThat(result.getUsername()).isEqualTo("alice.developer");
        assertThat(result.getEmail()).isEqualTo("alice.developer@example.com");
        assertThat(result.getRole()).isEqualTo(UserRole.DEVELOPER);
        assertThat(result.getManagerId()).isEqualTo(managerId);

        verify(userSnapshotRepository).findByUserId(developerId1);
    }

    @Test
    @DisplayName("Should throw exception when user not found by ID")
    void getUserById_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userSnapshotRepository.findByUserId(developerId1)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userSnapshotService.getUserById(developerId1))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("User not found with id: " + developerId1);

        verify(userSnapshotRepository).findByUserId(developerId1);
    }

    @Test
    @DisplayName("Should return true when user exists")
    void userExists_ShouldReturnTrue_WhenUserExists() {
        // Given
        when(userSnapshotRepository.findByUserId(developerId1)).thenReturn(Optional.of(developer1Snapshot));

        // When
        boolean result = userSnapshotService.userExists(developerId1);

        // Then
        assertThat(result).isTrue();

        verify(userSnapshotRepository).findByUserId(developerId1);
    }

    @Test
    @DisplayName("Should return false when user does not exist")
    void userExists_ShouldReturnFalse_WhenUserDoesNotExist() {
        // Given
        when(userSnapshotRepository.findByUserId(developerId1)).thenReturn(Optional.empty());

        // When
        boolean result = userSnapshotService.userExists(developerId1);

        // Then
        assertThat(result).isFalse();

        verify(userSnapshotRepository).findByUserId(developerId1);
    }

    @Test
    @DisplayName("Should handle reassignment when developer already has different manager")
    void assignDeveloperToManager_ShouldReassign_WhenDeveloperHasDifferentManager() {
        // Given
        UUID oldManagerId = UUID.randomUUID();
        UUID newManagerId = UUID.randomUUID();
        
        UserSnapshot developerWithOldManager = UserSnapshot.builder()
                .id(UUID.randomUUID())
                .userId(developerId1)
                .managerId(oldManagerId)
                .fullName("Alice Developer")
                .username("alice.developer")
                .email("alice.developer@example.com")
                .role(UserRole.DEVELOPER)
                .build();
        
        UserSnapshot newManager = UserSnapshot.builder()
                .id(UUID.randomUUID())
                .userId(newManagerId)
                .fullName("New Manager")
                .username("new.manager")
                .email("new.manager@example.com")
                .role(UserRole.MANAGER)
                .build();

        when(userSnapshotRepository.findByUserId(developerId1)).thenReturn(Optional.of(developerWithOldManager));
        when(userSnapshotRepository.findByUserId(newManagerId)).thenReturn(Optional.of(newManager));
        when(userSnapshotRepository.save(any(UserSnapshot.class))).thenReturn(developerWithOldManager);

        // When
        UserSnapshotDto result = userSnapshotService.assignDeveloperToManager(developerId1, newManagerId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(developerId1);
        assertThat(result.getManagerId()).isEqualTo(newManagerId);

        verify(userSnapshotRepository).findByUserId(developerId1);
        verify(userSnapshotRepository).findByUserId(newManagerId);
        verify(userSnapshotRepository).save(developerWithOldManager);
        
        // Verify the developer's managerId was updated
        assertThat(developerWithOldManager.getManagerId()).isEqualTo(newManagerId);
    }
}