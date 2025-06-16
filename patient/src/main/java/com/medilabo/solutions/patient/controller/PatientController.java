package com.medilabo.solutions.patient.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.medilabo.solutions.patient.dto.PatientDto;
import com.medilabo.solutions.patient.exception.ResourceNotFoundException;
import com.medilabo.solutions.patient.service.PatientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    @Autowired
    private PatientService patientService;

    /**
     * Récupère tous les patients
     * 
     * @return Liste de tous les patients
     */
    @GetMapping
    public ResponseEntity<List<PatientDto>> getAllPatients() {
        List<PatientDto> patients = patientService.findAll();
        return ResponseEntity.ok(patients);
    }

    /**
     * Retrieves a paginated list of all patients with sorting capabilities.
     * 
     * @param page    the page number to retrieve (0-based indexing, defaults to 0)
     * @param size    the number of patients per page (defaults to 10)
     * @param sortBy  the field name to sort by (defaults to "id")
     * @param sortDir the sort direction, either "asc" or "desc" (defaults to "asc")
     * @return ResponseEntity containing a Page of PatientDto objects with HTTP 200
     *         status
     */
    @GetMapping("/page")
    public ResponseEntity<Page<PatientDto>> getAllPatients(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<PatientDto> patients = patientService.findAll(pageable);

        return ResponseEntity.ok(patients);
    }

    /**
     * Récupère un patient par son ID
     * 
     * @param id L'identifiant du patient
     * @return Le patient correspondant à l'ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PatientDto> getPatient(@PathVariable int id) {
        PatientDto patient = patientService.findById(id);
        return ResponseEntity.ok(patient);
    }

    /**
     * Crée un nouveau patient
     * 
     * @param patientDto Les données du patient à créer
     * @return Le patient créé
     */
    @PostMapping
    public ResponseEntity<PatientDto> createPatient(@Valid @RequestBody PatientDto patientDto) {
        PatientDto createdPatient = patientService.create(patientDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPatient);
    }

    /**
     * Met à jour un patient existant
     * 
     * @param id         L'identifiant du patient à mettre à jour
     * @param patientDto Les nouvelles données du patient
     * @return Le patient mis à jour
     */
    @PutMapping("/{id}")
    public ResponseEntity<PatientDto> updatePatient(@PathVariable int id, @Valid @RequestBody PatientDto patientDto) {
        // Vérifier que le patient existe d'abord
        patientService.findById(id);

        // S'assurer que l'ID du DTO correspond à celui du path
        patientDto.setId(id);

        PatientDto updatedPatient = patientService.update(patientDto);
        return ResponseEntity.ok(updatedPatient);
    }

    /**
     * Deletes a patient from the system by its ID.
     *
     * @param id the unique identifier of the patient to delete
     * @return a ResponseEntity with HTTP status 204 (No Content) if deletion was
     *         successful
     * @throws ResourceNotFoundException if no patient exists with the given ID
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable int id) {
        PatientDto patientToDelete = patientService.findById(id);
        patientService.delete(patientToDelete);
        return ResponseEntity.noContent().build();
    }
}