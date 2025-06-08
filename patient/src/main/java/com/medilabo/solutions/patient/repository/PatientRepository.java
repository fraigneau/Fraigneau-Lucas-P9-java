package com.medilabo.solutions.patient.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medilabo.solutions.patient.model.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

}
