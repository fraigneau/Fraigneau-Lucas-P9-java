package com.medilabo.solutions.patient.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medilabo.solutions.patient.model.Patient;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Integer> {

    Page<Patient> findAll(Pageable pageable);

}
