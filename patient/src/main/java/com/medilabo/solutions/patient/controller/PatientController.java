package com.medilabo.solutions.patient.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medilabo.solutions.patient.dto.PatientDto;
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
     * Supprime un patient
     * 
     * @param id L'identifiant du patient à supprimer
     * @return Réponse vide avec statut 204 (No Content)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable int id) {
        PatientDto patientToDelete = patientService.findById(id);
        patientService.delete(patientToDelete);
        return ResponseEntity.noContent().build();
    }
}