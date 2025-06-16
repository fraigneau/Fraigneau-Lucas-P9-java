package com.medilabo.solutions.front.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.medilabo.solutions.front.dto.NoteDto;
import com.medilabo.solutions.front.dto.PatientDto;
import com.medilabo.solutions.front.dto.PatientPageDto;

@FeignClient(name = "gateway")
public interface GatewayServiceClient {

    @GetMapping("/api/patient")
    List<PatientDto> getAllPatients();

    @GetMapping("/api/patient/page")
    PatientPageDto getAllPatients(@RequestParam("page") int page, 
                                     @RequestParam("size") int size, 
                                     @RequestParam("sortBy") String sortBy, 
                                     @RequestParam("sortDir") String sortDir);

    @GetMapping("/api/patient/{id}")
    PatientDto getPatientById(@PathVariable("id") Long patientId);

    @PostMapping("/api/patient")
    PatientDto createPatient(PatientDto patientDto);

    @PutMapping("/api/patient/{id}")
    PatientDto updatePatient(@PathVariable("id") Long patientId, PatientDto patientDto);

    @GetMapping("/api/note/{patId}")
    List<NoteDto> getNoteByPatientId(@PathVariable("patId") int patId);

    @PostMapping("/api/note")
    NoteDto createNote(NoteDto noteDto);

    @GetMapping("api/assessment/{id}")
    String getAssessmentById(@PathVariable("id") Long assessmentId);
}
