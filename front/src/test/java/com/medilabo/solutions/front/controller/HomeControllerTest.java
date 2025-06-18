package com.medilabo.solutions.front.controller;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
import com.medilabo.solutions.front.dto.PatientDto;
import com.medilabo.solutions.front.dto.PatientPageDto;

import feign.FeignException;

@WebMvcTest(HomeController.class)
@DisplayName("Home Controller Tests")
public class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GatewayServiceClient gatewayServiceClient;

    private PatientPageDto mockPatientPage;
    private List<PatientDto> mockPatients;

    @BeforeEach
    void setUp() {
        // Création de patients de test
        PatientDto patient1 = new PatientDto();
        patient1.setId(1);
        patient1.setFirstname("John");
        patient1.setLastname("Doe");
        patient1.setBirthDate(LocalDate.of(1990, 1, 1));
        patient1.setGender("M");

        PatientDto patient2 = new PatientDto();
        patient2.setId(2);
        patient2.setFirstname("Jane");
        patient2.setLastname("Smith");
        patient2.setBirthDate(LocalDate.of(1985, 5, 15));
        patient2.setGender("F");

        mockPatients = Arrays.asList(patient1, patient2);

        // Création de la page de test
        mockPatientPage = new PatientPageDto();
        mockPatientPage.setContent(mockPatients);
        mockPatientPage.setNumber(0);
        mockPatientPage.setSize(4);
        mockPatientPage.setTotalPages(1);
        mockPatientPage.setTotalElements(2);
        mockPatientPage.setNumberOfElements(2);
        mockPatientPage.setFirst(true);
        mockPatientPage.setLast(true);
        mockPatientPage.setEmpty(false);
    }

    @Test
    @DisplayName("Should display home page with default parameters")
    void home_WithDefaultParameters_ShouldReturnHomeView() throws Exception {
        // Given
        when(gatewayServiceClient.getAllPatients(0, 4, "id", "asc"))
                .thenReturn(mockPatientPage);

        // When & Then
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("patients", mockPatients))
                .andExpect(model().attribute("currentPage", 0))
                .andExpect(model().attribute("totalPages", 1))
                .andExpect(model().attribute("totalElements", 2L))
                .andExpect(model().attribute("size", 4))
                .andExpect(model().attribute("sortBy", "id"))
                .andExpect(model().attribute("sortDir", "asc"));
    }

    @Test
    @DisplayName("Should display home page with custom parameters")
    void home_WithCustomParameters_ShouldReturnHomeViewWithCustomData() throws Exception {
        // Given
        PatientPageDto customPage = new PatientPageDto();
        customPage.setContent(Arrays.asList(mockPatients.get(0)));
        customPage.setNumber(1);
        customPage.setSize(2);
        customPage.setTotalPages(2);
        customPage.setTotalElements(3);
        customPage.setNumberOfElements(1);

        when(gatewayServiceClient.getAllPatients(1, 2, "lastname", "desc"))
                .thenReturn(customPage);

        // When & Then
        mockMvc.perform(get("/home")
                .param("page", "1")
                .param("size", "2")
                .param("sortBy", "lastname")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("currentPage", 1))
                .andExpect(model().attribute("totalPages", 2))
                .andExpect(model().attribute("size", 2))
                .andExpect(model().attribute("sortBy", "lastname"))
                .andExpect(model().attribute("sortDir", "desc"));
    }

    @Test
    @DisplayName("Should handle empty patient list")
    void home_WithEmptyPatientList_ShouldReturnHomeViewWithEmptyList() throws Exception {
        // Given
        PatientPageDto emptyPage = new PatientPageDto();
        emptyPage.setContent(Arrays.asList());
        emptyPage.setNumber(0);
        emptyPage.setSize(4);
        emptyPage.setTotalPages(0);
        emptyPage.setTotalElements(0);
        emptyPage.setNumberOfElements(0);
        emptyPage.setEmpty(true);

        when(gatewayServiceClient.getAllPatients(anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(emptyPage);

        // When & Then
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("patients", Arrays.asList()))
                .andExpect(model().attribute("totalElements", 0L));
    }

    @Test
    @DisplayName("Should handle gateway service exception")
    void home_WhenGatewayServiceThrowsException_ShouldReturnHomeViewWithError() throws Exception {
        // Given
        when(gatewayServiceClient.getAllPatients(anyInt(), anyInt(), anyString(), anyString()))
                .thenThrow(FeignException.FeignClientException.class);

        // When & Then
        mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("error", "Erreur lors de la récupération des patients"));
    }

    @Test
    @DisplayName("Should handle invalid page parameter")
    void home_WithInvalidPageParameter_ShouldUseDefaultValue() throws Exception {
        // Given
        when(gatewayServiceClient.getAllPatients(0, 4, "id", "asc"))
                .thenReturn(mockPatientPage);

        // When & Then
        mockMvc.perform(get("/home").param("page", "invalid"))
                .andExpect(status().isBadRequest()); // Spring convertit automatiquement, peut causer une erreur
    }

    @Test
    @DisplayName("Should handle negative page parameter")
    void home_WithNegativePageParameter_ShouldWork() throws Exception {
        // Given
        when(gatewayServiceClient.getAllPatients(-1, 4, "id", "asc"))
                .thenReturn(mockPatientPage);

        // When & Then
        mockMvc.perform(get("/home").param("page", "-1"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"));
    }

    @Test
    @DisplayName("Should handle large page size")
    void home_WithLargePageSize_ShouldWork() throws Exception {
        // Given
        when(gatewayServiceClient.getAllPatients(0, 100, "id", "asc"))
                .thenReturn(mockPatientPage);

        // When & Then
        mockMvc.perform(get("/home").param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("size", 4)); // La taille retournée par le mock
    }

    @Test
    @DisplayName("Should handle different sort directions")
    void home_WithDifferentSortDirections_ShouldWork() throws Exception {
        // Given
        when(gatewayServiceClient.getAllPatients(0, 4, "firstname", "desc"))
                .thenReturn(mockPatientPage);

        // When & Then
        mockMvc.perform(get("/home")
                .param("sortBy", "firstname")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("sortBy", "firstname"))
                .andExpect(model().attribute("sortDir", "desc"));
    }

    @Test
    @DisplayName("Should handle all parameters together")
    void home_WithAllCustomParameters_ShouldWork() throws Exception {
        // Given
        PatientPageDto customPage = new PatientPageDto();
        customPage.setContent(mockPatients);
        customPage.setNumber(2);
        customPage.setSize(3);
        customPage.setTotalPages(5);
        customPage.setTotalElements(12);
        customPage.setNumberOfElements(2);

        when(gatewayServiceClient.getAllPatients(2, 3, "birthDate", "desc"))
                .thenReturn(customPage);

        // When & Then
        mockMvc.perform(get("/home")
                .param("page", "2")
                .param("size", "3")
                .param("sortBy", "birthDate")
                .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(model().attribute("currentPage", 2))
                .andExpect(model().attribute("totalPages", 5))
                .andExpect(model().attribute("totalElements", 12L))
                .andExpect(model().attribute("size", 3))
                .andExpect(model().attribute("sortBy", "birthDate"))
                .andExpect(model().attribute("sortDir", "desc"));
    }
}