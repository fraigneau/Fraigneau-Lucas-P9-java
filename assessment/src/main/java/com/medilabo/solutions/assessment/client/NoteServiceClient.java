package com.medilabo.solutions.assessment.client;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.medilabo.solutions.assessment.dto.NoteDto;

@FeignClient(name = "note")
public interface NoteServiceClient {

    @GetMapping("/api/note/{patId}")
    List<NoteDto> getNoteByPatientId(@PathVariable("patId") int patId);
    
} 
