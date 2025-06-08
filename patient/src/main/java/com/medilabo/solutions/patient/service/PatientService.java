package com.medilabo.solutions.patient.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.medilabo.solutions.patient.dto.PatientDto;
import com.medilabo.solutions.patient.exception.ResourceNotFoundException;
import com.medilabo.solutions.patient.mapper.PatientMapper;
import com.medilabo.solutions.patient.model.Patient;
import com.medilabo.solutions.patient.repository.PatientRepository;

@Service
public class PatientService implements CrudService<PatientDto> {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientService(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    @Override
    public PatientDto create(PatientDto patientDto) {
        Patient patient = patientMapper.toEntity(patientDto);
        return patientMapper.toDto(patientRepository.save(patient));
    }

    @Override
    public List<PatientDto> findAll() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public PatientDto findById(int id) {
        return patientMapper.toDto(patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id)));
    }

    @Override
    public PatientDto update(PatientDto patientDto) {
        Patient patient = patientMapper.toEntity(patientDto);
        return patientMapper.toDto(patientRepository.save(patient));
    }

    @Override
    public void delete(PatientDto patientDto) {
        Patient patient = patientMapper.toEntity(patientDto);
        patientRepository.delete(patient);
    }

}
