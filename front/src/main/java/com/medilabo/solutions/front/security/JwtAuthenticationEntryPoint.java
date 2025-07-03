package com.medilabo.solutions.front.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT authentication entry point that handles unauthorized access attempts.
 * 
 * This class is called when a user attempts to access a protected resource
 * without being authenticated or with an invalid JWT token.
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    /**
     * Determines if the request is an API request.
     * 
     * @param request the HTTP request
     * @return true if it's an API request, false otherwise
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        return requestURI.startsWith("/api/");
    }

    /**
     * Method called when an authentication exception is thrown.
     * 
     * @param request       the HTTP request that caused the authentication
     *                      exception
     * @param response      the HTTP response
     * @param authException the authentication exception that was thrown
     * @throws IOException      in case of I/O error
     * @throws ServletException in case of servlet error
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        logger.warn("Unauthorized access to {} {} - {}", method, requestURI, authException.getMessage());

        // For AJAX or API requests, return JSON
        if (isApiRequest(request)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        } else {
            // For normal requests, redirect to login page
            response.sendRedirect("/login");
        }
    }
}