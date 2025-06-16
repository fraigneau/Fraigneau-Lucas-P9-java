package com.medilabo.solutions.front.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.medilabo.solutions.front.client.GatewayServiceClient;
import com.medilabo.solutions.front.dto.PatientDto;

import jakarta.validation.Valid;

@Controller
public class PatientFormController {

    private static final Logger logger = LoggerFactory.getLogger(PatientFormController.class);

    @Autowired
    private GatewayServiceClient gatewayServiceClient;


    /**
     * Displays the form for creating a new patient.
     * 
     * This method handles GET requests to "/patient/new" and prepares the model
     * with a new empty PatientDto object and the necessary attributes for rendering
     * the patient creation form.
     * 
     * @param model the Spring Model object used to pass attributes to the view
     * @return the name of the view template "patient-form" to be rendered
     */
    @GetMapping("/patient/new")
    public String showNewPatientForm(Model model) {
        PatientDto patient = new PatientDto();
        model.addAttribute("patient", patient);
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Nouveau Patient");
        return "patientform";
    }


    /**
     * Displays the edit form for an existing patient.
     * 
     * @param patientId the unique identifier of the patient to edit
     * @param model the Spring Model object to pass data to the view
     * @return the name of the patient form view template, or redirects to home on error
     * 
     * @throws Exception if patient retrieval fails or patient is not found
     * 
     * This method retrieves patient data by ID and prepares the model with:
     * - patient: the PatientDto object containing patient information
     * - isEdit: boolean flag set to true indicating edit mode
     * - pageTitle: localized title for the edit form
     * 
     * On success, returns "patient-form" view. On error, logs the exception
     * and redirects to "/home" with an error message.
     */
    @GetMapping("/patient/{id}/edit")
    public String showEditPatientForm(@PathVariable("id") Long patientId, Model model) {
        try {
            PatientDto patient = gatewayServiceClient.getPatientById(patientId);
            model.addAttribute("patient", patient);
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Modifier Patient");
            logger.info("Loaded patient {} for editing", patientId);
        } catch (Exception e) {
            logger.error("Error loading patient {} for editing: {}", patientId, e.getMessage());
            model.addAttribute("error", "Erreur lors du chargement du patient");
            return "redirect:/home";
        }
        return "patientform";
    }


    /**
     * Handles the form submission for saving a patient (create or update operation).
     * 
     * This method processes patient data from a form submission, validates the input,
     * and either creates a new patient or updates an existing one based on whether
     * the patient ID is present.
     * 
     * @param patientDto The patient data transfer object containing form data, validated with @Valid
     * @param bindingResult The result of the validation process, contains any validation errors
     * @param model The Spring MVC model for adding attributes to the view
     * @param redirectAttributes Attributes to be passed during redirect operations
     * @return String representing the view name or redirect URL:
     *         - "patient-form" if validation errors occur or an exception is thrown
     *         - "redirect:/home" if the patient is successfully saved
     * 
     * @throws Exception if there's an error during the save operation (caught and handled internally)
     * 
     * The method performs the following operations:
     * - Validates the patient data and returns to form view if validation fails
     * - Determines if this is a create or update operation based on patient ID
     * - Calls the appropriate gateway service method (create or update)
     * - Adds success/error messages to flash attributes or model
     * - Logs the operation results
     * - Redirects to home page on success or returns to form on error
     */
    @PostMapping("/patient/save")
    public String savePatient(@Valid @ModelAttribute("patient") PatientDto patientDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", patientDto.getId() != 0);
            model.addAttribute("pageTitle", patientDto.getId() != 0 ? "Modifier Patient" : "Nouveau Patient");
            return "patientform";
        }

        try {
            boolean isEdit = patientDto.getId() != 0;

            if (isEdit) {
                gatewayServiceClient.updatePatient((long) patientDto.getId(), patientDto);
                redirectAttributes.addFlashAttribute("success", "Patient mis à jour avec succès");
                logger.info("Successfully updated patient {}", patientDto.getId());
            } else {
                PatientDto savedPatient = gatewayServiceClient.createPatient(patientDto);
                redirectAttributes.addFlashAttribute("success", "Patient créé avec succès");
                logger.info("Successfully created new patient with ID {}", savedPatient.getId());
            }

            return "redirect:/home";

        } catch (Exception e) {
            logger.error("Error saving patient: {}", e.getMessage());
            model.addAttribute("error", "Erreur lors de l'enregistrement du patient");
            model.addAttribute("isEdit", patientDto.getId() != 0);
            model.addAttribute("pageTitle", patientDto.getId() != 0 ? "Modifier Patient" : "Nouveau Patient");
            return "patientform";
        }
    }
}