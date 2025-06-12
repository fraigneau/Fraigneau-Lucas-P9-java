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
import com.medilabo.solutions.assessment.dto.Risk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class AssessmentService {

    private final PatientServiceClient patientServiceClient;
    private final NoteServiceClient noteServiceClient;

    // TODO refaire la liste des termes déclencheurs
    private static final List<String> TRIGGER_TERMS = List.of(
            "Hémoglobine A1C", "HbA1c", "A1C", "Hémoglobine glyquée",

            "Microalbumine", "Micro-albumine", "Microalbuminurie", "Albuminurie",

            "Taille", "Hauteur", "Stature", "Grandeur", "Mesure corporelle",

            "Poids", "Masse", "Corpulent", "Corpulente", "Obèse", "Obésité",
            "Maigre", "Maigreur", "Surpoids", "IMC", "BMI", "Indice de masse",

            "Fumeur", "Fumeuse", "Fume", "Fumé", "Tabac", "Cigarette",
            "Nicotine", "Tabagique", "Tabagisme", "Tabacologie",

            "Anormal", "Anormale", "Anormalement", "Anormalité", "Pathologique",
            "Dysfonction", "Trouble", "Anomalie", "Irrégulier", "Irrégulière",

            "Cholestérol", "Cholesterol", "LDL", "HDL", "Lipide", "Lipidique",
            "Triglycéride", "Hypercholestérolémie", "Dyslipidémie",

            "Vertige", "Vertiges", "Étourdissement", "Étourdissements", "Malaise",
            "Instabilité", "Déséquilibre", "Tournis",

            "Rechute", "Récidive", "Récidivant", "Récurrence", "Réapparition",
            "Retour", "Rechuter", "Récidiver",

            "Réaction", "Réactivité", "Réactif", "Réactive", "Allergie",
            "Allergique", "Intolérance", "Sensibilité", "Hypersensibilité",

            "Anticorps", "Immunoglobuline", "IgG", "IgM", "IgA", "IgE",
            "Immunité", "Défense immunitaire", "Système immunitaire");

    public Risk createAssessment(int patId) {
        log.info("Creating assessment for patient ID: {}", patId);

        PatientDto patientDto = patientServiceClient.getPatientById(Long.valueOf(patId));
        List<NoteDto> notes = noteServiceClient.getNoteByPatientId(patId);

        Risk risk = new Risk();
        risk.setPatientId(patId);
        risk.setBirthDate(patientDto.getBirthDate());
        risk.setGender(patientDto.getGender());
        risk.setNotes(notes.stream().map(NoteDto::getNote).toList());

        return risk;
    }

    public DiabetesRiskLevelEnum assessDiabetesRisk(Risk risk) {
        int age = calculateAge(risk.getBirthDate());
        boolean isMale = "M".equals(risk.getGender());
        int triggerCount = countTriggerTerms(risk.getNotes());

        DiabetesRiskLevelEnum riskLevel = calculateRiskLevel(age, isMale, triggerCount);

        log.info("Assessment completed - Age: {}, Gender: {}, Triggers: {}, Risk: {}",
                age, risk.getGender(), triggerCount, riskLevel);

        return riskLevel;
    }

    private int calculateAge(LocalDate birthDate) {
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    private int countTriggerTerms(List<String> notes) {
        return (int) notes.stream()
                .flatMap(note -> TRIGGER_TERMS.stream()
                        .filter(term -> note.toLowerCase().contains(term.toLowerCase())))
                .distinct()
                .count();
    }

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