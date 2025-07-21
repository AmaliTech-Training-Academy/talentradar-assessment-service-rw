package com.talentradar.assessment_service.service;

import com.talentradar.assessment_service.dto.userSnapshot.response.UserSnapshotDto;

import java.util.List;
import java.util.UUID;

public interface UserSnapshotService {
    
    /**
     * Get all developers assigned to a specific manager
     * @param managerId the ID of the manager
     * @return list of developers assigned to the manager
     */
    List<UserSnapshotDto> getDevelopersByManagerId(UUID managerId);
    
    /**
     * Assign a developer to a manager
     * @param developerId the ID of the developer to assign
     * @param managerId the ID of the manager to assign to
     * @return the updated developer user snapshot
     */
    UserSnapshotDto assignDeveloperToManager(UUID developerId, UUID managerId);
    
    /**
     * Get all developers who are not assigned to any manager
     * @return list of unassigned developers
     */
    List<UserSnapshotDto> getUnassignedDevelopers();
    
    /**
     * Get user by ID
     * @param userId the ID of the user
     * @return user snapshot
     */
    UserSnapshotDto getUserById(UUID userId);
    
    /**
     * Check if user exists
     * @param userId the ID of the user
     * @return true if user exists, false otherwise
     */
    boolean userExists(UUID userId);
}