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
import com.medilabo.solutions.front.dto.NoteDto;

import jakarta.validation.Valid;
import java.util.List;

@Controller
public class NotesController {

    private static final Logger logger = LoggerFactory.getLogger(NotesController.class);

    @Autowired
    private GatewayServiceClient gatewayServiceClient;

    @GetMapping("/notes/{id}")
    public String getPatientNotes(@PathVariable("id") Long patientId, Model model) {
        try {
            PatientDto patient = gatewayServiceClient.getPatientById(patientId);
            model.addAttribute("patient", patient);

            List<NoteDto> notes = gatewayServiceClient.getNoteByPatientId(patientId.intValue());
            model.addAttribute("notes", notes);

            String riskAssessment = gatewayServiceClient.getAssessmentById(patientId);
            model.addAttribute("riskLevel", riskAssessment);

            NoteDto newNote = new NoteDto();
            newNote.setPatId(patientId.intValue());
            model.addAttribute("newNote", newNote);

            logger.info("Successfully retrieved patient {} with {} notes", patientId, notes.size());

        } catch (Exception e) {
            logger.error("Error retrieving patient notes for ID {}: {}", patientId, e.getMessage());
            model.addAttribute("error", "Erreur lors de la récupération des données du patient");
        }

        return "notes";
    }

    @PostMapping("/notes/{id}")
    public String addNote(@PathVariable("id") Long patientId,
            @Valid @ModelAttribute("newNote") NoteDto noteDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("noteError", "Veuillez corriger les erreurs dans le formulaire");
            return "redirect:/notes/" + patientId;
        }

        try {
            // Récupérer les informations du patient pour obtenir son nom
            PatientDto patient = gatewayServiceClient.getPatientById(patientId);

            // Ensure the noteDto has the correct patient ID and patient name
            noteDto.setId(null); // Ne pas définir l'ID, il sera généré automatiquement
            noteDto.setPatId(patientId.intValue());
            noteDto.setPatient(patient.getFirstname());

            logger.info("Note to be added: {}", noteDto);

            gatewayServiceClient.createNote(noteDto);
            redirectAttributes.addFlashAttribute("success", "Note ajoutée avec succès");
            logger.info("Successfully added note for patient {}", patientId);

        } catch (Exception e) {
            logger.error("Error adding note for patient {}: {}", patientId, e.getMessage());
            redirectAttributes.addFlashAttribute("noteError", "Erreur lors de l'ajout de la note");
        }

        return "redirect:/notes/" + patientId;
    }

    @PostMapping("/patient/{id}/update")
    public String updatePatient(@PathVariable("id") Long patientId,
            @Valid @ModelAttribute("patient") PatientDto patientDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("patientError", "Veuillez corriger les erreurs dans le formulaire");
            return "redirect:/notes/" + patientId;
        }

        try {
            gatewayServiceClient.updatePatient(patientId, patientDto);
            redirectAttributes.addFlashAttribute("success", "Informations patient mises à jour avec succès");
            logger.info("Successfully updated patient {}", patientId);

        } catch (Exception e) {
            logger.error("Error updating patient {}: {}", patientId, e.getMessage());
            redirectAttributes.addFlashAttribute("patientError", "Erreur lors de la mise à jour des informations");
        }

        return "redirect:/notes/" + patientId;
    }
}