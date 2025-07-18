package com.talentradar.assessment_service.mapper;

import com.talentradar.assessment_service.dto.assessment.response.AssessmentResponseDTO;
import com.talentradar.assessment_service.dto.assessment.response.DimensionResponseDTO;
import com.talentradar.assessment_service.model.Assessment;
import com.talentradar.assessment_service.model.AssessmentDimension;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AssessmentMapper {

    // Entity to DTO
    @Mapping(source = "submissionStatus", target = "status")
    @Mapping(source = "averageScore", target = "average")
    AssessmentResponseDTO toResponseDto(Assessment entity);

    DimensionResponseDTO toDimensionDto(AssessmentDimension dimension);
    List<DimensionResponseDTO> toDimensionDtoList(List<AssessmentDimension> dimensions);

}
