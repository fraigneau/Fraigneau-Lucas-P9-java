package com.medilabo.solutions.patient.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.medilabo.solutions.patient.dto.PatientDto;
import com.medilabo.solutions.patient.model.Patient;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface PatientMapper {

    public PatientDto toDto(Patient patient);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public Patient toEntity(PatientDto patientDto);

}