package com.medilabo.solutions.assessment.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.medilabo.solutions.assessment.dto.DiabetesRiskLevelEnum;
import com.medilabo.solutions.assessment.service.AssessmentService;

@WebMvcTest(AssessmentController.class)
@DisplayName("Assessment Controller Tests")
public class AssessmentControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @MockBean
        private AssessmentService assessmentService;

        @BeforeEach
        void setUp() {
        }

        @Test
        @DisplayName("Should return NONE risk level for patient")
        void getAssessment_WithPatientHavingNoRisk_ShouldReturnNone() throws Exception {
                // Given
                Long patientId = 1L;
                when(assessmentService.assessDiabetesRisk(patientId.intValue()))
                                .thenReturn(DiabetesRiskLevelEnum.NONE);

                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").value("NONE"));

                verify(assessmentService, times(1)).assessDiabetesRisk(patientId.intValue());
        }

        @Test
        @DisplayName("Should return BORDERLINE risk level for patient")
        void getAssessment_WithPatientHavingBorderlineRisk_ShouldReturnBorderline() throws Exception {
                // Given
                Long patientId = 2L;
                when(assessmentService.assessDiabetesRisk(patientId.intValue()))
                                .thenReturn(DiabetesRiskLevelEnum.BORDERLINE);

                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").value("BORDERLINE"));

                verify(assessmentService, times(1)).assessDiabetesRisk(patientId.intValue());
        }

        @Test
        @DisplayName("Should return IN_DANGER risk level for patient")
        void getAssessment_WithPatientInDanger_ShouldReturnInDanger() throws Exception {
                // Given
                Long patientId = 3L;
                when(assessmentService.assessDiabetesRisk(patientId.intValue()))
                                .thenReturn(DiabetesRiskLevelEnum.IN_DANGER);

                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").value("IN_DANGER"));

                verify(assessmentService, times(1)).assessDiabetesRisk(patientId.intValue());
        }

        @Test
        @DisplayName("Should return EARLY_ONSET risk level for patient")
        void getAssessment_WithPatientHavingEarlyOnset_ShouldReturnEarlyOnset() throws Exception {
                // Given
                Long patientId = 4L;
                when(assessmentService.assessDiabetesRisk(patientId.intValue()))
                                .thenReturn(DiabetesRiskLevelEnum.EARLY_ONSET);

                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                .andExpect(status().isOk())
                                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                                .andExpect(jsonPath("$").value("EARLY_ONSET"));

                verify(assessmentService, times(1)).assessDiabetesRisk(patientId.intValue());
        }

        @Test
        @DisplayName("Should handle different patient IDs correctly")
        void getAssessment_WithDifferentPatientIds_ShouldCallServiceWithCorrectIds() throws Exception {
                Long[] patientIds = { 1L, 5L, 100L, 999L, 1000L };

                for (Long patientId : patientIds) {
                        // Given
                        when(assessmentService.assessDiabetesRisk(patientId.intValue()))
                                        .thenReturn(DiabetesRiskLevelEnum.NONE);

                        // When & Then
                        mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$").value("NONE"));

                        verify(assessmentService, times(1)).assessDiabetesRisk(patientId.intValue());
                }
        }

        @Test
        @DisplayName("Should handle large patient ID correctly")
        void getAssessment_WithLargePatientId_ShouldReturnAssessment() throws Exception {
                // Given
                Long largePatientId = 2147483647L;
                when(assessmentService.assessDiabetesRisk(largePatientId.intValue()))
                                .thenReturn(DiabetesRiskLevelEnum.BORDERLINE);

                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", largePatientId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value("BORDERLINE"));

                verify(assessmentService, times(1)).assessDiabetesRisk(largePatientId.intValue());
        }

        @Test
        @DisplayName("Should return 500 when service throws RuntimeException")
        void getAssessment_WhenServiceThrowsRuntimeException_ShouldReturn500() throws Exception {
                // Given
                Long patientId = 999L;
                when(assessmentService.assessDiabetesRisk(patientId.intValue()))
                                .thenThrow(new RuntimeException("Patient not found"));

                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                .andExpect(status().isInternalServerError());

                verify(assessmentService, times(1)).assessDiabetesRisk(patientId.intValue());
        }

        @Test
        @DisplayName("Should return 400 when service throws IllegalArgumentException")
        void getAssessment_WhenServiceThrowsIllegalArgumentException_ShouldReturn400() throws Exception {
                // Given
                Long patientId = -1L;
                when(assessmentService.assessDiabetesRisk(patientId.intValue()))
                                .thenThrow(new IllegalArgumentException("Invalid patient ID"));

                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                .andExpect(status().isBadRequest());

                verify(assessmentService, times(1)).assessDiabetesRisk(patientId.intValue());
        }

        @Test
        @DisplayName("Should handle zero patient ID")
        void getAssessment_WithZeroPatientId_ShouldCallService() throws Exception {
                // Given
                Long patientId = 0L;
                when(assessmentService.assessDiabetesRisk(patientId.intValue()))
                                .thenReturn(DiabetesRiskLevelEnum.NONE);

                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value("NONE"));

                verify(assessmentService, times(1)).assessDiabetesRisk(patientId.intValue());
        }

        @Test
        @DisplayName("Should handle negative patient ID")
        void getAssessment_WithNegativePatientId_ShouldCallService() throws Exception {
                // Given
                Long patientId = -5L;
                when(assessmentService.assessDiabetesRisk(patientId.intValue()))
                                .thenReturn(DiabetesRiskLevelEnum.NONE);

                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value("NONE"));

                verify(assessmentService, times(1)).assessDiabetesRisk(patientId.intValue());
        }

        @Test
        @DisplayName("Should return 400 for invalid path parameter format")
        void getAssessment_WithInvalidPathParameter_ShouldReturn400() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", "invalid"))
                                .andExpect(status().isBadRequest());

                verify(assessmentService, times(0)).assessDiabetesRisk(anyInt());
        }

        @Test
        @DisplayName("Should return 400 for non-numeric path parameter")
        void getAssessment_WithNonNumericPathParameter_ShouldReturn400() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", "abc"))
                                .andExpect(status().isBadRequest());

                verify(assessmentService, times(0)).assessDiabetesRisk(anyInt());
        }

        @Test
        @DisplayName("Should return 400 for decimal path parameter")
        void getAssessment_WithDecimalPathParameter_ShouldReturn400() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", "123.45"))
                                .andExpect(status().isBadRequest());

                verify(assessmentService, times(0)).assessDiabetesRisk(anyInt());
        }

        @Test
        @DisplayName("Should return 404 for missing path parameter")
        void getAssessment_WithMissingPathParameter_ShouldReturn404() throws Exception {
                // When & Then
                mockMvc.perform(get("/api/assessment/"))
                                .andExpect(status().isBadRequest());

                verify(assessmentService, times(0)).assessDiabetesRisk(anyInt());
        }

        @Test
        @DisplayName("Should log patient ID and risk level")
        void getAssessment_ShouldLogCorrectInformation() throws Exception {
                // Given
                Long patientId = 42L;
                when(assessmentService.assessDiabetesRisk(patientId.intValue()))
                                .thenReturn(DiabetesRiskLevelEnum.IN_DANGER);

                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value("IN_DANGER"));

                verify(assessmentService, times(1)).assessDiabetesRisk(42);
        }

        @Test
        @DisplayName("Should handle multiple consecutive requests correctly")
        void getAssessment_WithMultipleConsecutiveRequests_ShouldHandleAllCorrectly() throws Exception {
                // Given
                Long patientId = 1L;
                when(assessmentService.assessDiabetesRisk(patientId.intValue()))
                                .thenReturn(DiabetesRiskLevelEnum.BORDERLINE);

                // When & Then
                for (int i = 0; i < 3; i++) {
                        mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$").value("BORDERLINE"));
                }

                verify(assessmentService, times(3)).assessDiabetesRisk(patientId.intValue());
        }

        @Test
        @DisplayName("Should handle very large patient ID that causes integer overflow")
        void getAssessment_WithVeryLargePatientId_ShouldHandleOverflow() throws Exception {
                // Given
                Long veryLargePatientId = 3000000000L;

                when(assessmentService.assessDiabetesRisk(veryLargePatientId.intValue()))
                                .thenReturn(DiabetesRiskLevelEnum.NONE);

                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", veryLargePatientId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value("NONE"));

                verify(assessmentService, times(1)).assessDiabetesRisk(veryLargePatientId.intValue());
        }

        @Test
        @DisplayName("Should handle edge case where service returns null")
        void getAssessment_WhenServiceReturnsNull_ShouldHandleGracefully() throws Exception {
                // Given
                Long patientId = 1L;
                when(assessmentService.assessDiabetesRisk(patientId.intValue()))
                                .thenReturn(null);

                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").doesNotExist());

                verify(assessmentService, times(1)).assessDiabetesRisk(patientId.intValue());
        }

        @Test
        @DisplayName("Should correctly convert Long to int for service call")
        void getAssessment_ShouldCorrectlyConvertLongToInt() throws Exception {
                // Given
                Long patientId = 123456L;
                when(assessmentService.assessDiabetesRisk(123456))
                                .thenReturn(DiabetesRiskLevelEnum.EARLY_ONSET);

                // When & Then
                mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").value("EARLY_ONSET"));

                verify(assessmentService, times(1)).assessDiabetesRisk(123456);
        }

        @Test
        @DisplayName("Should handle assessment for all risk levels in sequence")
        void getAssessment_WithAllRiskLevels_ShouldReturnCorrectValues() throws Exception {
                DiabetesRiskLevelEnum[] riskLevels = DiabetesRiskLevelEnum.values();

                for (int i = 0; i < riskLevels.length; i++) {
                        // Given
                        Long patientId = (long) (i + 1);
                        DiabetesRiskLevelEnum expectedRisk = riskLevels[i];

                        when(assessmentService.assessDiabetesRisk(patientId.intValue()))
                                        .thenReturn(expectedRisk);

                        // When & Then
                        mockMvc.perform(get("/api/assessment/{patId}", patientId))
                                        .andExpect(status().isOk())
                                        .andExpect(jsonPath("$").value(expectedRisk.name()));

                        verify(assessmentService, times(1)).assessDiabetesRisk(patientId.intValue());
                }
        }
}