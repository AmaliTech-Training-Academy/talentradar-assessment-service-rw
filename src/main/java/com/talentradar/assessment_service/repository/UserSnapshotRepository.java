package com.talentradar.assessment_service.repository;

import com.talentradar.assessment_service.model.UserSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserSnapshotRepository extends JpaRepository<UserSnapshot, UUID> {
    Optional<UserSnapshot> findByUserId(UUID userId);

}
