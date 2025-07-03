package com.medilabo.solutions.front.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.medilabo.solutions.front.dto.LoginRequest;
import com.medilabo.solutions.front.util.JwtUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

/**
 * Controller handling user authentication and JWT token generation.
 * 
 * This controller provides:
 * - A login page
 * - An authentication endpoint that generates JWT tokens
 * - Cookie management to store tokens
 */
@Controller
@Slf4j
public class LoginController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Displays the login page.
     * 
     * @param model the Spring MVC model
     * @return the login view name
     */
    @GetMapping("/login")
    public String showLoginPage(Model model) {
        model.addAttribute("loginRequest", new LoginRequest());
        return "login";
    }

    /**
     * Processes the login request and generates a JWT token.
     * 
     * @param loginRequest       the login data
     * @param bindingResult      the validation result
     * @param model              the Spring MVC model
     * @param response           the HTTP response to set cookies
     * @param redirectAttributes the redirect attributes
     * @return redirect to home page or return to login page
     */
    @PostMapping("/login")
    public String authenticate(@Valid @ModelAttribute("loginRequest") LoginRequest loginRequest,
            BindingResult bindingResult,
            Model model,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "login";
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());

            String jwt = jwtUtil.generateToken(userDetails);

            Cookie jwtCookie = new Cookie("jwt", jwt);
            jwtCookie.setHttpOnly(true); 
            jwtCookie.setSecure(false);
            jwtCookie.setPath("/");

            response.addCookie(jwtCookie);

            log.info("User '{}' logged in successfully", loginRequest.getUsername());

            redirectAttributes.addFlashAttribute("success", "Connexion réussie !");
            return "redirect:/home";

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user '{}': {}",
                    loginRequest.getUsername(), e.getMessage());
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
            return "login";
        }
    }
}
