package com.medilabo.solutions.front.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.medilabo.solutions.front.client.GatewayServiceClient;
import com.medilabo.solutions.front.dto.PatientPageDto;

@Controller
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    @Autowired
    private GatewayServiceClient gatewayServiceClient;

    @Autowired
    private CacheManager cacheManager;

    /**
     * Handles GET requests to the "/home" endpoint and displays a paginated list
     * of patients.
     * 
     * This method retrieves patients from the gateway service with pagination and
     * sorting support,
     * then adds the necessary attributes to the model for rendering in the "home"
     * view.
     * 
     * @param model   the Spring MVC model object used to pass data to the view
     * @param page    the page number to retrieve (0-based, defaults to 0)
     * @param size    the number of patients per page (defaults to 2)
     * @param sortBy  the field to sort by (defaults to "id")
     * @param sortDir the sort direction, either "asc" or "desc" (defaults to "asc")
     * @return the name of the view template ("home") to render
     * 
     * @throws Exception if an error occurs while retrieving patients from the
     *                   gateway service
     */
    @GetMapping("/home")
    @Cacheable(value = "patient")
    public String home(Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        // Affichage du contenu du cache
        if (cacheManager != null) {
            org.springframework.cache.Cache cache = cacheManager.getCache("patient");
            if (cache != null) {
                logger.debug("Cache 'patient' est présent: {}", cache.getName());
                logger.debug("Cache native store: {}", cache.getNativeCache());
            } else {
                logger.debug("Cache 'patient' non trouvé");
            }
        }

        try {
            PatientPageDto patientPageDto = gatewayServiceClient.getAllPatients(page, size, sortBy, sortDir);

            model.addAttribute("patients", patientPageDto.getContent());
            model.addAttribute("currentPage", patientPageDto.getNumber());
            model.addAttribute("totalPages", patientPageDto.getTotalPages());
            model.addAttribute("totalElements", patientPageDto.getTotalElements());
            model.addAttribute("size", patientPageDto.getSize());
            model.addAttribute("sortBy", sortBy);
            model.addAttribute("sortDir", sortDir);

            logger.info("Successfully retrieved page {} with {} patients", page, patientPageDto.getNumberOfElements());

        } catch (Exception e) {
            logger.error("Error retrieving patients with pagination: {}", e.getMessage());
            model.addAttribute("error", "Erreur lors de la récupération des patients");
        }

        return "home";
    }

}