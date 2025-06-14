package com.medilabo.solutions.assessment.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.stereotype.Service;

import com.medilabo.solutions.assessment.client.NoteServiceClient;
import com.medilabo.solutions.assessment.client.PatientServiceClient;
import com.medilabo.solutions.assessment.dto.DiabetesRiskLevelEnum;
import com.medilabo.solutions.assessment.dto.NoteDto;
import com.medilabo.solutions.assessment.dto.PatientDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AssessmentService {

    private final PatientServiceClient patientServiceClient;
    private final NoteServiceClient noteServiceClient;

    private static final List<String> TRIGGER_TERMS = List.of(
            "hémoglobine a1c",
            "microalbumine",
            "taille",
            "poids",
            "fumeur",
            "anormal",
            "anormale",
            "cholestérol",
            "vertige",
            "réaction",
            "anticorps");

    /**
     * Assesses the diabetes risk level for a given patient based on their age,
     * gender, and medical notes.
     * 
     * This method retrieves patient information and medical notes, then calculates
     * the diabetes risk
     * by analyzing trigger terms in the notes and applying risk assessment rules
     * based on age and gender.
     * 
     * @param patId the unique identifier of the patient to assess
     * @return the calculated diabetes risk level as a DiabetesRiskLevelEnum
     * @throws RuntimeException if patient data cannot be retrieved or if the
     *                          patient ID is invalid
     * 
     * @see DiabetesRiskLevelEnum
     * @see PatientDto
     * @see NoteDto
     */
    public DiabetesRiskLevelEnum assessDiabetesRisk(int patId) {
        log.info("Creating assessment for patient ID: {}", patId);

        PatientDto patientDto = patientServiceClient.getPatientById(Long.valueOf(patId));
        List<NoteDto> notes = noteServiceClient.getNoteByPatientId(patId);

        int age = calculateAge(patientDto.getBirthDate());
        boolean isMale = "M".equals(patientDto.getGender());
        List<String> noteTexts = notes.stream().map(NoteDto::getNote).toList();
        int triggerCount = countTriggerTerms(noteTexts);

        DiabetesRiskLevelEnum riskLevel = calculateRiskLevel(age, isMale, triggerCount);

        log.info("Assessment completed - Patient ID: {}, Age: {}, Gender: {}, Triggers: {}, Risk: {}",
                patId, age, patientDto.getGender(), triggerCount, riskLevel);

        return riskLevel;
    }

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    /**
     * Counts the number of distinct trigger terms found in the provided list of
     * notes.
     * 
     * This method performs a case-insensitive search through all notes to identify
     * trigger terms that are present. Each trigger term is counted only once,
     * regardless of how many times it appears across all notes.
     * 
     * @param notes the list of note strings to search through for trigger terms
     * @return the count of distinct trigger terms found in the notes
     */
    private int countTriggerTerms(List<String> notes) {
        return (int) notes.stream()
                .flatMap(note -> TRIGGER_TERMS.stream()
                        .filter(term -> note.toLowerCase().contains(term.toLowerCase())))
                .distinct()
                .count();
    }

    /**
     * Calculates the diabetes risk level based on patient demographics and trigger
     * count.
     * 
     * The risk assessment follows different criteria based on age and gender:
     * - For patients over 30: Risk is determined solely by trigger count
     * - For patients 30 or under: Risk is determined by trigger count with
     * different thresholds for males and females
     * 
     * Risk levels are determined as follows:
     * - NONE: No triggers present, or trigger count doesn't meet minimum thresholds
     * - BORDERLINE: Only applies to patients over 30 with 2-5 triggers
     * - IN_DANGER:
     * - Patients over 30: 6-7 triggers
     * - Males 30 or under: 3-4 triggers
     * - Females 30 or under: 4-6 triggers
     * - EARLY_ONSET:
     * - Patients over 30: 8 or more triggers
     * - Males 30 or under: 5 or more triggers
     * - Females 30 or under: 7 or more triggers
     * 
     * @param age          the patient's age in years
     * @param isMale       true if the patient is male, false if female
     * @param triggerCount the number of diabetes risk factor triggers identified
     * @return the calculated diabetes risk level as a DiabetesRiskLevelEnum
     */
    private DiabetesRiskLevelEnum calculateRiskLevel(int age, boolean isMale, int triggerCount) {
        if (triggerCount == 0) {
            return DiabetesRiskLevelEnum.NONE;
        }

        if (age > 30) {
            if (triggerCount >= 2 && triggerCount <= 5) {
                return DiabetesRiskLevelEnum.BORDERLINE;
            } else if (triggerCount >= 6 && triggerCount <= 7) {
                return DiabetesRiskLevelEnum.IN_DANGER;
            } else if (triggerCount >= 8) {
                return DiabetesRiskLevelEnum.EARLY_ONSET;
            }
        } else {
            if (isMale) {
                if (triggerCount >= 3 && triggerCount <= 4) {
                    return DiabetesRiskLevelEnum.IN_DANGER;
                } else if (triggerCount >= 5) {
                    return DiabetesRiskLevelEnum.EARLY_ONSET;
                }
            } else {
                if (triggerCount >= 4 && triggerCount <= 6) {
                    return DiabetesRiskLevelEnum.IN_DANGER;
                } else if (triggerCount >= 7) {
                    return DiabetesRiskLevelEnum.EARLY_ONSET;
                }
            }
        }

        return DiabetesRiskLevelEnum.NONE;
    }
}