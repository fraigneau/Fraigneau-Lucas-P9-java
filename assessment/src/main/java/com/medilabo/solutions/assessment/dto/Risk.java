package com.medilabo.solutions.assessment.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Data;

@Data
public class Risk {

    private int patientId;
    private LocalDate birthDate;
    private String gender;

    private List<String> notes;

}
