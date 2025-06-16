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
     * Affiche le formulaire pour créer un nouveau patient
     */
    @GetMapping("/patient/new")
    public String showNewPatientForm(Model model) {
        PatientDto patient = new PatientDto();
        model.addAttribute("patient", patient);
        model.addAttribute("isEdit", false);
        model.addAttribute("pageTitle", "Nouveau Patient");
        return "patient-form";
    }

    /**
     * Affiche le formulaire pour modifier un patient existant
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
        return "patient-form";
    }

    /**
     * Traite la soumission du formulaire (création ou mise à jour)
     */
    @PostMapping("/patient/save")
    public String savePatient(@Valid @ModelAttribute("patient") PatientDto patientDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Vérification des erreurs de validation
        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", patientDto.getId() != 0);
            model.addAttribute("pageTitle", patientDto.getId() != 0 ? "Modifier Patient" : "Nouveau Patient");
            return "patient-form";
        }

        try {
            boolean isEdit = patientDto.getId() != 0;

            if (isEdit) {
                // Mise à jour d'un patient existant
                gatewayServiceClient.updatePatient((long) patientDto.getId(), patientDto);
                redirectAttributes.addFlashAttribute("success", "Patient mis à jour avec succès");
                logger.info("Successfully updated patient {}", patientDto.getId());
            } else {
                // Création d'un nouveau patient
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
            return "patient-form";
        }
    }
}