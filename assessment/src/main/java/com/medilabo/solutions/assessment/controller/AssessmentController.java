package com.medilabo.solutions.assessment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.medilabo.solutions.assessment.dto.DiabetesRiskLevelEnum;
import com.medilabo.solutions.assessment.dto.Risk;
import com.medilabo.solutions.assessment.service.AssessmentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assessment")
public class AssessmentController {

    private final AssessmentService assessmentService;

    @GetMapping("/{patId}")
    public ResponseEntity<DiabetesRiskLevelEnum> getAssessment(@PathVariable Long patId) {

        Risk risk = assessmentService.createAssessment(patId.intValue());
        DiabetesRiskLevelEnum riskLevel = assessmentService.assessDiabetesRisk(risk);

        log.info("Risk level for patient {}: {}", patId, riskLevel);

        return ResponseEntity.ok(riskLevel);
    }

}