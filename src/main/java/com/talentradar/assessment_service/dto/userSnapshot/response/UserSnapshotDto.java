package com.talentradar.assessment_service.dto.userSnapshot.response;

import com.talentradar.assessment_service.model.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSnapshotDto {
    private UUID id;
    private UUID userId;
    private UUID managerId;
    private String fullName;
    private String username;
    private String email;
    private UserRole role;
}