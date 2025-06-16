package com.medilabo.solutions.front.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.medilabo.solutions.front.dto.NoteDto;
import com.medilabo.solutions.front.dto.PatientDto;

@FeignClient(name = "gateway")
public interface GatewayServiceClient {

    @GetMapping("/api/note/{patId}")
    List<NoteDto> getNoteByPatientId(@PathVariable("patId") int patId);

    @GetMapping("/api/patient/{id}")
    PatientDto getPatientById(@PathVariable("id") Long patientId);

    @GetMapping("api/assessment/{id}")
    String getAssessmentById(@PathVariable("id") Long assessmentId);

    @PostMapping("/api/note/{patId}")
    NoteDto createNote(NoteDto noteDto);

    @PostMapping("/api/patient/{id}")
    PatientDto createPatient(PatientDto patientDto);

}
