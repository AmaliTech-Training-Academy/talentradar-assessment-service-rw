package com.talentradar.assessment_service.controller;

import com.talentradar.assessment_service.dto.api.ApiResponse;
import com.talentradar.assessment_service.dto.userSnapshot.request.AssignDeveloperToManagerDto;
import com.talentradar.assessment_service.dto.userSnapshot.response.UserSnapshotDto;
import com.talentradar.assessment_service.service.UserSnapshotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserSnapshotController {

    private final UserSnapshotService userSnapshotService;

    @GetMapping("/developers")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<UserSnapshotDto>>> getDevelopersByManager(
            @RequestHeader("X-User-Id") UUID managerId) {
        List<UserSnapshotDto> developers = userSnapshotService.getDevelopersByManagerId(managerId);
        return ResponseEntity.ok(
            ApiResponse.success(developers, "Developers retrieved successfully")
        );
    }

    @PatchMapping("/developers/{developerId}/assign")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserSnapshotDto>> assignDeveloperToManager(
            @PathVariable UUID developerId,
            @RequestHeader("X-User-Id") UUID managerId,
            @Valid @RequestBody(required = false) AssignDeveloperToManagerDto assignDto) {
        
        // Use managerId from header if not provided in body, or validate consistency
        UUID targetManagerId = (assignDto != null && assignDto.getManagerId() != null) 
            ? assignDto.getManagerId() 
            : managerId;
            
        UserSnapshotDto updatedDeveloper = userSnapshotService.assignDeveloperToManager(developerId, targetManagerId);
        return ResponseEntity.ok(
            ApiResponse.success(updatedDeveloper, "Developer assigned to manager successfully")
        );
    }

    @GetMapping("/developers/unassigned")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<List<UserSnapshotDto>>> getUnassignedDevelopers() {
        List<UserSnapshotDto> unassignedDevelopers = userSnapshotService.getUnassignedDevelopers();
        return ResponseEntity.ok(
            ApiResponse.success(unassignedDevelopers, "Unassigned developers retrieved successfully")
        );
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('MANAGER') or hasRole('DEVELOPER')")
    public ResponseEntity<ApiResponse<UserSnapshotDto>> getUserById(@PathVariable UUID userId) {
        UserSnapshotDto user = userSnapshotService.getUserById(userId);
        return ResponseEntity.ok(
            ApiResponse.success(user, "User retrieved successfully")
        );
    }
}