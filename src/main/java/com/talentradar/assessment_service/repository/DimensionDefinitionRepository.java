package com.talentradar.assessment_service.repository;

import com.talentradar.assessment_service.model.DimensionDefinition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DimensionDefinitionRepository extends JpaRepository<DimensionDefinition, UUID> {

    @Query("SELECT dd.id FROM DimensionDefinition dd WHERE dd.id IN :ids")
    List<UUID> findExistingIds(@Param("ids") List<UUID> ids); //Validate that all provided IDs exist
}
