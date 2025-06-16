package com.medilabo.solutions.front.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.medilabo.solutions.front.client.GatewayServiceClient;
import com.medilabo.solutions.front.dto.PatientDto;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private GatewayServiceClient gatewayServiceClient;

    /**
     * Handles GET requests to the home page and displays a list of all patients.
     * 
     * This method retrieves all patients from the gateway service and adds them to the model
     * for display on the home page. If an error occurs during retrieval, an error message
     * is added to the model instead.
     * 
     * @param model the Spring Model object used to pass data to the view
     * @return the name of the view template ("home") to be rendered
     */
    @GetMapping("/home")
    public String home(Model model) {
        try {
            List<PatientDto> patients = gatewayServiceClient.getAllPatients();

            model.addAttribute("patients", patients);

            logger.info("Successfully retrieved {} patients", patients.size());

        } catch (Exception e) {
            logger.error("Error retrieving patients: {}", e.getMessage());
            model.addAttribute("error", "Erreur lors de la récupération des patients");
        }

        return "home";
    }

}