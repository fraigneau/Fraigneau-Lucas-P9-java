package com.medilabo.solutions.patient.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.medilabo.solutions.patient.dto.PatientDto;
import com.medilabo.solutions.patient.exception.ResourceNotFoundException;
import com.medilabo.solutions.patient.mapper.PatientMapper;
import com.medilabo.solutions.patient.model.Patient;
import com.medilabo.solutions.patient.repository.PatientRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Patient Service Tests")
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientService patientService;

    private Patient patient;
    private PatientDto patientDto;
    private List<Patient> patientList;
    private List<PatientDto> patientDtoList;

    @BeforeEach
    void setUp() {
        // Setup Patient entity
        patient = new Patient();
        patient.setId(1);
        patient.setFirstname("John");
        patient.setLastname("Doe");
        patient.setBirthDate(LocalDate.of(1990, 1, 1));
        patient.setGender("M");
        patient.setAddress("123 Main St");
        patient.setPhoneNumber("0123456789");
        patient.setCreatedAt(LocalDateTime.now());
        patient.setUpdatedAt(LocalDateTime.now());

        // Setup PatientDto
        patientDto = new PatientDto();
        patientDto.setId(1);
        patientDto.setFirstname("John");
        patientDto.setLastname("Doe");
        patientDto.setBirthDate(LocalDate.of(1990, 1, 1));
        patientDto.setGender("M");
        patientDto.setAddress("123 Main St");
        patientDto.setPhoneNumber("0123456789");

        // Setup lists
        Patient patient2 = new Patient();
        patient2.setId(2);
        patient2.setFirstname("Jane");
        patient2.setLastname("Smith");
        patient2.setBirthDate(LocalDate.of(1985, 5, 15));
        patient2.setGender("F");

        PatientDto patientDto2 = new PatientDto();
        patientDto2.setId(2);
        patientDto2.setFirstname("Jane");
        patientDto2.setLastname("Smith");
        patientDto2.setBirthDate(LocalDate.of(1985, 5, 15));
        patientDto2.setGender("F");

        patientList = Arrays.asList(patient, patient2);
        patientDtoList = Arrays.asList(patientDto, patientDto2);
    }

    @Test
    @DisplayName("Should create patient successfully")
    void create_WithValidPatientDto_ShouldReturnCreatedPatient() {
        // Given
        when(patientMapper.toEntity(patientDto)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(patient);
        when(patientMapper.toDto(patient)).thenReturn(patientDto);

        // When
        PatientDto result = patientService.create(patientDto);

        // Then
        assertNotNull(result);
        assertEquals(patientDto.getId(), result.getId());
        assertEquals(patientDto.getFirstname(), result.getFirstname());
        verify(patientMapper).toEntity(patientDto);
        verify(patientRepository).save(patient);
        verify(patientMapper).toDto(patient);
    }

    @Test
    @DisplayName("Should find all patients")
    void findAll_ShouldReturnAllPatients() {
        // Given
        when(patientRepository.findAll()).thenReturn(patientList);
        when(patientMapper.toDto(patient)).thenReturn(patientDto);
        when(patientMapper.toDto(patientList.get(1))).thenReturn(patientDtoList.get(1));

        // When
        List<PatientDto> result = patientService.findAll();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(patientRepository).findAll();
        verify(patientMapper, times(2)).toDto(any(Patient.class));
    }

    @Test
    @DisplayName("Should find all patients with pagination")
    void findAllWithPagination_ShouldReturnPaginatedPatients() {
        // Given
        Pageable pageable = PageRequest.of(0, 2);
        Page<Patient> patientPage = new PageImpl<>(patientList, pageable, patientList.size());
        when(patientRepository.findAll(pageable)).thenReturn(patientPage);
        when(patientMapper.toDto(any(Patient.class))).thenReturn(patientDto);

        // When
        Page<PatientDto> result = patientService.findAll(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(0, result.getNumber());
        assertEquals(2, result.getSize());
        verify(patientRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Should find patient by ID")
    void findById_WithValidId_ShouldReturnPatient() {
        // Given
        when(patientRepository.findById(1)).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(patientDto);

        // When
        PatientDto result = patientService.findById(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals("John", result.getFirstname());
        verify(patientRepository).findById(1);
        verify(patientMapper).toDto(patient);
    }

    @Test
    @DisplayName("Should throw exception when patient not found by ID")
    void findById_WithInvalidId_ShouldThrowException() {
        // Given
        when(patientRepository.findById(999)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> patientService.findById(999));

        assertEquals("Patient not found with id: 999", exception.getMessage());
        verify(patientRepository).findById(999);
    }

    @Test
    @DisplayName("Should update patient successfully")
    void update_WithValidPatientDto_ShouldReturnUpdatedPatient() {
        // Given
        PatientDto updatedPatientDto = new PatientDto();
        updatedPatientDto.setId(1);
        updatedPatientDto.setFirstname("John Updated");
        updatedPatientDto.setLastname("Doe Updated");
        updatedPatientDto.setBirthDate(LocalDate.of(1990, 1, 1));
        updatedPatientDto.setGender("M");

        Patient updatedPatient = new Patient();
        updatedPatient.setId(1);
        updatedPatient.setFirstname("John Updated");
        updatedPatient.setLastname("Doe Updated");

        when(patientMapper.toEntity(updatedPatientDto)).thenReturn(updatedPatient);
        when(patientRepository.save(updatedPatient)).thenReturn(updatedPatient);
        when(patientMapper.toDto(updatedPatient)).thenReturn(updatedPatientDto);

        // When
        PatientDto result = patientService.update(updatedPatientDto);

        // Then
        assertNotNull(result);
        assertEquals("John Updated", result.getFirstname());
        verify(patientMapper).toEntity(updatedPatientDto);
        verify(patientRepository).save(updatedPatient);
        verify(patientMapper).toDto(updatedPatient);
    }

    @Test
    @DisplayName("Should delete patient successfully")
    void delete_WithValidPatientDto_ShouldDeletePatient() {
        // Given
        when(patientMapper.toEntity(patientDto)).thenReturn(patient);

        // When
        patientService.delete(patientDto);

        // Then
        verify(patientMapper).toEntity(patientDto);
        verify(patientRepository).delete(patient);
    }
}