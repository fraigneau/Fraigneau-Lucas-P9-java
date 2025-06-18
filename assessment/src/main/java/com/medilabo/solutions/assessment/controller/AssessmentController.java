package com.medilabo.solutions.assessment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medilabo.solutions.assessment.dto.DiabetesRiskLevelEnum;
import com.medilabo.solutions.assessment.service.AssessmentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assessment")
public class AssessmentController {

    private final AssessmentService assessmentService;

    /**
     * Retrieves the diabetes risk assessment for a specific patient.
     * 
     * @param patId the unique identifier of the patient for whom to assess diabetes
     *              risk
     * @return ResponseEntity containing the DiabetesRiskLevelEnum representing the
     *         patient's diabetes risk level
     * @throws IllegalArgumentException if the patient ID is invalid or patient not
     *                                  found
     */
    @GetMapping("/{patId}")
    public ResponseEntity<DiabetesRiskLevelEnum> getAssessment(@PathVariable Long patId) {
        log.info("Requesting diabetes risk assessment for patient ID: {}", patId);

        try {
            DiabetesRiskLevelEnum riskLevel = assessmentService.assessDiabetesRisk(patId.intValue());
            log.info("Risk level for patient {}: {}", patId, riskLevel);
            return ResponseEntity.ok(riskLevel);
        } catch (IllegalArgumentException e) {
            log.error("Invalid patient ID or patient not found: {}", patId, e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error assessing diabetes risk for patient {}", patId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}