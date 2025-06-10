package com.medilabo.solutions.patient.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.medilabo.solutions.patient.dto.PatientDto;
import com.medilabo.solutions.patient.exception.ResourceNotFoundException;
import com.medilabo.solutions.patient.mapper.PatientMapper;
import com.medilabo.solutions.patient.model.Patient;
import com.medilabo.solutions.patient.repository.PatientRepository;

/**
 * Service class for managing patient data operations in the medical system.
 *
 * This service implements the CrudService interface for PatientDto objects and
 * provides
 * the core business logic for patient management operations including creating,
 * reading,
 * updating, and deleting patient records. It acts as an intermediary between
 * the controller
 * layer and the repository layer, applying necessary transformations between
 * DTOs and entities.
 *
 * The service uses PatientRepository for database operations and PatientMapper
 * for
 * conversion between Patient entities and PatientDto objects.
 *
 * @author Fraigneau Lucas
 * @version 1.0
 */
@Service
public class PatientService implements CrudService<PatientDto> {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientService(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    /**
     * Creates a new patient.
     * 
     * This method converts the provided PatientDto to a Patient entity,
     * saves it in the repository, and then returns the saved patient
     * converted back to a DTO.
     * 
     * @param patientDto The patient data transfer object containing the information
     *                   to create a new patient
     * @return The created PatientDto with updated information (like assigned ID)
     */
    @Override
    public PatientDto create(PatientDto patientDto) {
        Patient patient = patientMapper.toEntity(patientDto);
        return patientMapper.toDto(patientRepository.save(patient));
    }

    /**
     * Retrieves all patients from the database.
     * 
     * This method fetches all patient records, converts them to DTOs using the
     * patient mapper,
     * and returns them as a list.
     *
     * @return A list of PatientDto objects representing all patients in the
     *         database
     */
    @Override
    public List<PatientDto> findAll() {
        return patientRepository.findAll().stream()
                .map(patientMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves a patient by their ID.
     * 
     * @param id the unique identifier of the patient to retrieve
     * @return the PatientDto object containing the patient's information
     * @throws ResourceNotFoundException if no patient is found with the given ID
     */
    @Override
    public PatientDto findById(int id) {
        return patientMapper.toDto(patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id)));
    }

    /**
     * Updates an existing Patient entity with the provided information in
     * PatientDto.
     * 
     * @param patientDto The data transfer object containing the updated patient
     *                   information
     * @return The data transfer object representing the updated patient
     * @throws IllegalArgumentException if the patient is not found
     * @throws DataAccessException      if there's an error during the database
     *                                  operation
     */
    @Override
    public PatientDto update(PatientDto patientDto) {
        Patient patient = patientMapper.toEntity(patientDto);
        return patientMapper.toDto(patientRepository.save(patient));
    }

    /**
     * Deletes a patient from the database.
     * 
     * @param patientDto the patient data transfer object to be deleted
     */
    @Override
    public void delete(PatientDto patientDto) {
        Patient patient = patientMapper.toEntity(patientDto);
        patientRepository.delete(patient);
    }

}
