package com.talentradar.assessment_service.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserContext {
    private UUID userId;
    private String fullName;
    private String username;
    private String email;
    private String role;
    private UUID managerId;
}
