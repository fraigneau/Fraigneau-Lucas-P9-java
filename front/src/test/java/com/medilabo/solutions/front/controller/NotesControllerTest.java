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
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.medilabo.solutions.front.client.GatewayServiceClient;
import com.medilabo.solutions.front.dto.NoteDto;
import com.medilabo.solutions.front.dto.PatientDto;

import feign.FeignException;

@WebMvcTest(NotesController.class)
@DisplayName("Notes Controller Tests")
public class NotesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GatewayServiceClient gatewayServiceClient;

    private PatientDto mockPatient;
    private List<NoteDto> mockNotes;

    @BeforeEach
    void setUp() {
        // Création d'un patient de test
        mockPatient = new PatientDto();
        mockPatient.setId(1);
        mockPatient.setFirstname("John");
        mockPatient.setLastname("Doe");
        mockPatient.setBirthDate(LocalDate.of(1990, 1, 1));
        mockPatient.setGender("M");
        mockPatient.setAddress("123 Test Street");
        mockPatient.setPhoneNumber("0123456789");

        // Création de notes de test
        NoteDto note1 = new NoteDto();
        note1.setId("1");
        note1.setPatId(1);
        note1.setPatient("John");
        note1.setNote("Première consultation");

        NoteDto note2 = new NoteDto();
        note2.setId("2");
        note2.setPatId(1);
        note2.setPatient("John");
        note2.setNote("Suivi médical");

        mockNotes = Arrays.asList(note1, note2);
    }

    @Test
    @DisplayName("Should display patient notes successfully")
    void getPatientNotes_WithValidPatientId_ShouldReturnNotesView() throws Exception {
        // Given
        Long patientId = 1L;
        String riskAssessment = "NONE";

        when(gatewayServiceClient.getPatientById(patientId)).thenReturn(mockPatient);
        when(gatewayServiceClient.getNoteByPatientId(patientId.intValue())).thenReturn(mockNotes);
        when(gatewayServiceClient.getAssessmentById(patientId)).thenReturn(riskAssessment);

        // When & Then
        mockMvc.perform(get("/notes/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("notes"))
                .andExpect(model().attribute("patient", mockPatient))
                .andExpect(model().attribute("notes", mockNotes))
                .andExpect(model().attribute("riskLevel", riskAssessment))
                .andExpect(model().attributeExists("newNote"));
    }

    @Test
    @DisplayName("Should display notes view with empty notes list")
    void getPatientNotes_WithEmptyNotesList_ShouldReturnNotesView() throws Exception {
        // Given
        Long patientId = 1L;
        String riskAssessment = "BORDERLINE";

        when(gatewayServiceClient.getPatientById(patientId)).thenReturn(mockPatient);
        when(gatewayServiceClient.getNoteByPatientId(patientId.intValue())).thenReturn(Arrays.asList());
        when(gatewayServiceClient.getAssessmentById(patientId)).thenReturn(riskAssessment);

        // When & Then
        mockMvc.perform(get("/notes/{id}", patientId))
                .andExpect(status().isOk())
                .andExpect(view().name("notes"))
                .andExpect(model().attribute("patient", mockPatient))
                .andExpect(model().attribute("notes", Arrays.asList()))
                .andExpect(model().attribute("riskLevel", riskAssessment));
    }

    @Test
    @DisplayName("Should add note successfully")
    void addNote_WithValidNote_ShouldRedirectWithSuccess() throws Exception {
        // Given
        Long patientId = 1L;
        String noteContent = "Nouvelle note médicale";

        when(gatewayServiceClient.getPatientById(patientId)).thenReturn(mockPatient);
        when(gatewayServiceClient.createNote(any(NoteDto.class))).thenReturn(new NoteDto());

        // When & Then
        mockMvc.perform(post("/notes/{id}", patientId)
                .param("note", noteContent))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes/" + patientId))
                .andExpect(flash().attribute("success", "Note ajoutée avec succès"));
    }

    @Test
    @DisplayName("Should handle validation error when adding note")
    void addNote_WithEmptyNote_ShouldRedirectWithError() throws Exception {
        // Given
        Long patientId = 1L;

        // When & Then
        mockMvc.perform(post("/notes/{id}", patientId)
                .param("note", "")) // Note vide
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes/" + patientId))
                .andExpect(flash().attribute("noteError", "Veuillez corriger les erreurs dans le formulaire"));
    }

    @Test
    @DisplayName("Should handle exception when adding note")
    void addNote_WhenServiceThrowsException_ShouldRedirectWithError() throws Exception {
        // Given
        Long patientId = 1L;
        String noteContent = "Nouvelle note médicale";

        when(gatewayServiceClient.getPatientById(patientId)).thenReturn(mockPatient);
        when(gatewayServiceClient.createNote(any(NoteDto.class)))
                .thenThrow(FeignException.FeignClientException.class);

        // When & Then
        mockMvc.perform(post("/notes/{id}", patientId)
                .param("note", noteContent))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes/" + patientId))
                .andExpect(flash().attribute("noteError", "Erreur lors de l'ajout de la note"));
    }

    @Test
    @DisplayName("Should update patient successfully")
    void updatePatient_WithValidData_ShouldRedirectWithSuccess() throws Exception {
        // Given
        Long patientId = 1L;

        when(gatewayServiceClient.updatePatient(anyLong(), any(PatientDto.class)))
                .thenReturn(mockPatient);

        // When & Then
        mockMvc.perform(post("/patient/{id}/update", patientId)
                .param("firstname", "John")
                .param("lastname", "Doe")
                .param("birthDate", "1990-01-01")
                .param("gender", "M")
                .param("address", "123 Test Street")
                .param("phoneNumber", "0123456789"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes/" + patientId))
                .andExpect(flash().attribute("success", "Informations patient mises à jour avec succès"));
    }

    @Test
    @DisplayName("Should handle validation error when updating patient")
    void updatePatient_WithInvalidData_ShouldRedirectWithError() throws Exception {
        // Given
        Long patientId = 1L;

        // When & Then
        mockMvc.perform(post("/patient/{id}/update", patientId)
                .param("firstname", "") // Prénom vide
                .param("lastname", "Doe")
                .param("birthDate", "1990-01-01")
                .param("gender", "M"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes/" + patientId))
                .andExpect(flash().attribute("patientError", "Veuillez corriger les erreurs dans le formulaire"));
    }

    @Test
    @DisplayName("Should handle exception when updating patient")
    void updatePatient_WhenServiceThrowsException_ShouldRedirectWithError() throws Exception {
        // Given
        Long patientId = 1L;

        when(gatewayServiceClient.updatePatient(anyLong(), any(PatientDto.class)))
                .thenThrow(FeignException.FeignClientException.class);

        // When & Then
        mockMvc.perform(post("/patient/{id}/update", patientId)
                .param("firstname", "John")
                .param("lastname", "Doe")
                .param("birthDate", "1990-01-01")
                .param("gender", "M"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/notes/" + patientId))
                .andExpect(flash().attribute("patientError", "Erreur lors de la mise à jour des informations"));
    }
}