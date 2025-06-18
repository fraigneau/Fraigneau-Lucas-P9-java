package com.medilabo.solutions.patient.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medilabo.solutions.patient.dto.PatientDto;
import com.medilabo.solutions.patient.exception.ResourceNotFoundException;
import com.medilabo.solutions.patient.service.PatientService;

@WebMvcTest(PatientController.class)
@DisplayName("Patient Controller Tests")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    private PatientDto patientDto;
    private List<PatientDto> patientList;

    @BeforeEach
    void setUp() {
        patientDto = new PatientDto();
        patientDto.setId(1);
        patientDto.setFirstname("John");
        patientDto.setLastname("Doe");
        patientDto.setBirthDate(LocalDate.of(1990, 1, 1));
        patientDto.setGender("M");
        patientDto.setAddress("123 Main St");
        patientDto.setPhoneNumber("0123456789");

        PatientDto patientDto2 = new PatientDto();
        patientDto2.setId(2);
        patientDto2.setFirstname("Jane");
        patientDto2.setLastname("Smith");
        patientDto2.setBirthDate(LocalDate.of(1985, 5, 15));
        patientDto2.setGender("F");
        patientDto2.setAddress("456 Oak Ave");
        patientDto2.setPhoneNumber("0987654321");

        patientList = Arrays.asList(patientDto, patientDto2);
    }

    @Test
    @DisplayName("Should return all patients")
    void getAllPatients_ShouldReturnAllPatients() throws Exception {
        // Given
        when(patientService.findAll()).thenReturn(patientList);

        // When & Then
        mockMvc.perform(get("/api/patient"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].firstname").value("John"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].firstname").value("Jane"));
    }

    @Test
    @DisplayName("Should return paginated patients")
    void getAllPatientsWithPagination_ShouldReturnPaginatedPatients() throws Exception {
        // Given
        Pageable pageable = PageRequest.of(0, 2);
        Page<PatientDto> patientPage = new PageImpl<>(patientList, pageable, patientList.size());
        when(patientService.findAll(any(Pageable.class))).thenReturn(patientPage);

        // When & Then
        mockMvc.perform(get("/api/patient/page")
                .param("page", "0")
                .param("size", "2")
                .param("sortBy", "id")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("Should return patient by ID")
    void getPatient_WithValidId_ShouldReturnPatient() throws Exception {
        // Given
        when(patientService.findById(1)).thenReturn(patientDto);

        // When & Then
        mockMvc.perform(get("/api/patient/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstname").value("John"))
                .andExpect(jsonPath("$.lastname").value("Doe"));
    }

    @Test
    @DisplayName("Should return 404 when patient not found")
    void getPatient_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        when(patientService.findById(999)).thenThrow(new ResourceNotFoundException("Patient not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/api/patient/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Resource Not Found"))
                .andExpect(jsonPath("$.message").value("Patient not found with id: 999"));
    }

    @Test
    @DisplayName("Should create patient successfully")
    void createPatient_WithValidData_ShouldCreatePatient() throws Exception {
        // Given
        PatientDto newPatient = new PatientDto();
        newPatient.setFirstname("Alice");
        newPatient.setLastname("Johnson");
        newPatient.setBirthDate(LocalDate.of(1995, 3, 20));
        newPatient.setGender("F");
        newPatient.setAddress("789 Pine St");
        newPatient.setPhoneNumber("0555123456");

        PatientDto createdPatient = new PatientDto();
        createdPatient.setId(3);
        createdPatient.setFirstname("Alice");
        createdPatient.setLastname("Johnson");
        createdPatient.setBirthDate(LocalDate.of(1995, 3, 20));
        createdPatient.setGender("F");
        createdPatient.setAddress("789 Pine St");
        createdPatient.setPhoneNumber("0555123456");

        when(patientService.create(any(PatientDto.class))).thenReturn(createdPatient);

        // When & Then
        mockMvc.perform(post("/api/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newPatient)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.firstname").value("Alice"));
    }

    @Test
    @DisplayName("Should return 400 for invalid patient data")
    void createPatient_WithInvalidData_ShouldReturn400() throws Exception {
        // Given
        PatientDto invalidPatient = new PatientDto();
        invalidPatient.setFirstname(""); // Invalid: blank firstname
        invalidPatient.setLastname("Doe");
        invalidPatient.setBirthDate(LocalDate.now().plusDays(1)); // Invalid: future date
        invalidPatient.setGender("X"); // Invalid: wrong gender

        // When & Then
        mockMvc.perform(post("/api/patient")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidPatient)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should update patient successfully")
    void updatePatient_WithValidData_ShouldUpdatePatient() throws Exception {
        // Given
        PatientDto updatedPatient = new PatientDto();
        updatedPatient.setId(1);
        updatedPatient.setFirstname("John Updated");
        updatedPatient.setLastname("Doe Updated");
        updatedPatient.setBirthDate(LocalDate.of(1990, 1, 1));
        updatedPatient.setGender("M");
        updatedPatient.setAddress("123 Updated St");
        updatedPatient.setPhoneNumber("0123456789");

        when(patientService.findById(1)).thenReturn(patientDto);
        when(patientService.update(any(PatientDto.class))).thenReturn(updatedPatient);

        // When & Then
        mockMvc.perform(put("/api/patient/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedPatient)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstname").value("John Updated"))
                .andExpect(jsonPath("$.address").value("123 Updated St"));
    }

    @Test
    @DisplayName("Should return 404 when updating non-existent patient")
    void updatePatient_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        when(patientService.findById(999)).thenThrow(new ResourceNotFoundException("Patient not found with id: 999"));

        // When & Then
        mockMvc.perform(put("/api/patient/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patientDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should delete patient successfully")
    void deletePatient_WithValidId_ShouldDeletePatient() throws Exception {
        // Given
        when(patientService.findById(1)).thenReturn(patientDto);
        doNothing().when(patientService).delete(any(PatientDto.class));

        // When & Then
        mockMvc.perform(delete("/api/patient/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent patient")
    void deletePatient_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        when(patientService.findById(999)).thenThrow(new ResourceNotFoundException("Patient not found with id: 999"));

        // When & Then
        mockMvc.perform(delete("/api/patient/999"))
                .andExpect(status().isNotFound());
    }
}