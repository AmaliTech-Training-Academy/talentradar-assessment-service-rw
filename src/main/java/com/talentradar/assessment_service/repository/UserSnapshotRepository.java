package com.talentradar.assessment_service.repository;

import com.talentradar.assessment_service.model.UserRole;
import com.talentradar.assessment_service.model.UserSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSnapshotRepository extends JpaRepository<UserSnapshot, UUID> {
    Optional<UserSnapshot> findByUserId(UUID userId);

    List<UserSnapshot> findByManagerIdAndRole(UUID managerId, UserRole role);

    List<UserSnapshot> findByManagerIdIsNullAndRole(UserRole role);

}
