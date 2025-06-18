package com.medilabo.solutions.front.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.medilabo.solutions.front.client.GatewayServiceClient;
import com.medilabo.solutions.front.dto.PatientDto;

import feign.FeignException;

@WebMvcTest(PatientFormController.class)
@DisplayName("Patient Form Controller Tests")
public class PatientFormControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GatewayServiceClient gatewayServiceClient;

    private PatientDto mockPatient;

    @BeforeEach
    void setUp() {
        mockPatient = new PatientDto();
        mockPatient.setId(1);
        mockPatient.setFirstname("John");
        mockPatient.setLastname("Doe");
        mockPatient.setBirthDate(LocalDate.of(1990, 1, 1));
        mockPatient.setGender("M");
        mockPatient.setAddress("123 Test Street");
        mockPatient.setPhoneNumber("0123456789");
    }

    @Test
    @DisplayName("Should display new patient form")
    void showNewPatientForm_ShouldReturnPatientFormView() throws Exception {
        // When & Then
        mockMvc.perform(get("/patient/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("patientform"))
                .andExpect(model().attributeExists("patient"))
                .andExpect(model().attribute("isEdit", false))
                .andExpect(model().attribute("pageTitle", "Nouveau Patient"));
    }

    @Test
    @DisplayName("Should display edit patient form")
    void showEditPatientForm_WithValidPatientId_ShouldReturnPatientFormView() throws Exception {
        // Given
        Long patientId = 1L;
        when(gatewayServiceClient.getPatientById(patientId)).thenReturn(mockPatient);

        // When & Then
        mockMvc.perform(get("/patient/{id}/edit", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("patientform"))
                .andExpect(model().attribute("patient", mockPatient))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attribute("pageTitle", "Modifier Patient"));
    }

    @Test
    @DisplayName("Should handle patient not found when editing")
    void showEditPatientForm_WhenPatientNotFound_ShouldRedirectToHome() throws Exception {
        // Given
        Long patientId = 999L;
        when(gatewayServiceClient.getPatientById(patientId))
                .thenThrow(FeignException.FeignClientException.class);

        // When & Then
        mockMvc.perform(get("/patient/{id}/edit", patientId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @DisplayName("Should save new patient successfully")
    void savePatient_WithValidNewPatient_ShouldRedirectToHomeWithSuccess() throws Exception {
        // Given
        PatientDto savedPatient = new PatientDto();
        savedPatient.setId(123);
        savedPatient.setFirstname("John");
        savedPatient.setLastname("Doe");

        when(gatewayServiceClient.createPatient(any(PatientDto.class))).thenReturn(savedPatient);

        // When & Then
        mockMvc.perform(post("/patient/save")
                .param("id", "0")
                .param("firstname", "John")
                .param("lastname", "Doe")
                .param("birthDate", "1990-01-01")
                .param("gender", "M")
                .param("address", "123 Test Street")
                .param("phoneNumber", "0123456789"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attribute("success", "Patient créé avec succès"));
    }

    @Test
    @DisplayName("Should update existing patient successfully")
    void savePatient_WithValidExistingPatient_ShouldRedirectToHomeWithSuccess() throws Exception {
        // Given
        when(gatewayServiceClient.updatePatient(anyLong(), any(PatientDto.class)))
                .thenReturn(mockPatient);

        // When & Then
        mockMvc.perform(post("/patient/save")
                .param("id", "1")
                .param("firstname", "John")
                .param("lastname", "Doe")
                .param("birthDate", "1990-01-01")
                .param("gender", "M")
                .param("address", "123 Updated Street")
                .param("phoneNumber", "0123456789"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attribute("success", "Patient mis à jour avec succès"));
    }

    @Test
    @DisplayName("Should handle validation errors when saving patient")
    void savePatient_WithInvalidData_ShouldReturnFormWithErrors() throws Exception {
        // When & Then
        mockMvc.perform(post("/patient/save")
                .param("id", "0")
                .param("firstname", "")
                .param("lastname", "")
                .param("birthDate", "2030-01-01")
                .param("gender", "X"))
                .andExpect(status().isOk())
                .andExpect(view().name("patientform"))
                .andExpect(model().attribute("isEdit", false))
                .andExpect(model().attribute("pageTitle", "Nouveau Patient"));
    }

    @Test
    @DisplayName("Should handle service exception when creating patient")
    void savePatient_WhenCreateThrowsException_ShouldReturnFormWithError() throws Exception {
        // Given
        when(gatewayServiceClient.createPatient(any(PatientDto.class)))
                .thenThrow(FeignException.FeignClientException.class);

        // When & Then
        mockMvc.perform(post("/patient/save")
                .param("id", "0")
                .param("firstname", "John")
                .param("lastname", "Doe")
                .param("birthDate", "1990-01-01")
                .param("gender", "M"))
                .andExpect(status().isOk())
                .andExpect(view().name("patientform"))
                .andExpect(model().attribute("error", "Erreur lors de l'enregistrement du patient"))
                .andExpect(model().attribute("isEdit", false))
                .andExpect(model().attribute("pageTitle", "Nouveau Patient"));
    }

    @Test
    @DisplayName("Should handle service exception when updating patient")
    void savePatient_WhenUpdateThrowsException_ShouldReturnFormWithError() throws Exception {
        // Given
        when(gatewayServiceClient.updatePatient(anyLong(), any(PatientDto.class)))
                .thenThrow(FeignException.FeignClientException.class);

        // When & Then
        mockMvc.perform(post("/patient/save")
                .param("id", "1")
                .param("firstname", "John")
                .param("lastname", "Doe")
                .param("birthDate", "1990-01-01")
                .param("gender", "M"))
                .andExpect(status().isOk())
                .andExpect(view().name("patientform"))
                .andExpect(model().attribute("error", "Erreur lors de l'enregistrement du patient"))
                .andExpect(model().attribute("isEdit", true))
                .andExpect(model().attribute("pageTitle", "Modifier Patient"));
    }

    @Test
    @DisplayName("Should handle invalid phone number format")
    void savePatient_WithInvalidPhoneNumber_ShouldReturnFormWithErrors() throws Exception {
        // When & Then
        mockMvc.perform(post("/patient/save")
                .param("id", "0")
                .param("firstname", "John")
                .param("lastname", "Doe")
                .param("birthDate", "1990-01-01")
                .param("gender", "M")
                .param("phoneNumber", "invalid-phone"))
                .andExpect(status().isOk())
                .andExpect(view().name("patientform"));
    }

    @Test
    @DisplayName("Should handle invalid name with special characters")
    void savePatient_WithInvalidName_ShouldReturnFormWithErrors() throws Exception {
        // When & Then
        mockMvc.perform(post("/patient/save")
                .param("id", "0")
                .param("firstname", "John123")
                .param("lastname", "Doe@#$")
                .param("birthDate", "1990-01-01")
                .param("gender", "M"))
                .andExpect(status().isOk())
                .andExpect(view().name("patientform"));
    }

    @Test
    @DisplayName("Should handle too long address")
    void savePatient_WithTooLongAddress_ShouldReturnFormWithErrors() throws Exception {
        // Given
        String longAddress = "A".repeat(300);
        // When & Then
        mockMvc.perform(post("/patient/save")
                .param("id", "0")
                .param("firstname", "John")
                .param("lastname", "Doe")
                .param("birthDate", "1990-01-01")
                .param("gender", "M")
                .param("address", longAddress))
                .andExpect(status().isOk())
                .andExpect(view().name("patientform"));
    }

    @Test
    @DisplayName("Should handle minimum valid data")
    void savePatient_WithMinimumValidData_ShouldCreatePatient() throws Exception {
        // Given
        PatientDto savedPatient = new PatientDto();
        savedPatient.setId(123);

        when(gatewayServiceClient.createPatient(any(PatientDto.class))).thenReturn(savedPatient);

        // When & Then
        mockMvc.perform(post("/patient/save")
                .param("id", "0")
                .param("firstname", "Jo")
                .param("lastname", "Do")
                .param("birthDate", "1990-01-01")
                .param("gender", "F"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attribute("success", "Patient créé avec succès"));
    }

    @Test
    @DisplayName("Should handle valid French phone number formats")
    void savePatient_WithValidFrenchPhoneNumbers_ShouldCreatePatient() throws Exception {
        // Given
        PatientDto savedPatient = new PatientDto();
        savedPatient.setId(123);

        when(gatewayServiceClient.createPatient(any(PatientDto.class))).thenReturn(savedPatient);

        mockMvc.perform(post("/patient/save")
                .param("id", "0")
                .param("firstname", "John")
                .param("lastname", "Doe")
                .param("birthDate", "1990-01-01")
                .param("gender", "M")
                .param("phoneNumber", "+33123456789"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"));
    }

    @Test
    @DisplayName("Should handle name with accents and apostrophes")
    void savePatient_WithAccentsAndApostrophes_ShouldCreatePatient() throws Exception {
        // Given
        PatientDto savedPatient = new PatientDto();
        savedPatient.setId(123);

        when(gatewayServiceClient.createPatient(any(PatientDto.class))).thenReturn(savedPatient);

        // When & Then
        mockMvc.perform(post("/patient/save")
                .param("id", "0")
                .param("firstname", "François-René")
                .param("lastname", "d'Orléans")
                .param("birthDate", "1990-01-01")
                .param("gender", "M"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andExpect(flash().attribute("success", "Patient créé avec succès"));
    }
}