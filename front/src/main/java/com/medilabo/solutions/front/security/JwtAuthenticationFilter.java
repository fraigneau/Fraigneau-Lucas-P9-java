package com.medilabo.solutions.front.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.medilabo.solutions.front.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT authentication filter that extracts and validates JWT tokens from cookies.
 * This filter runs once per request and handles authentication based on JWT tokens.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final String JWT_COOKIE_NAME = "jwt";

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Processes the incoming request to extract and validate JWT token from cookies.
     * Sets up authentication context if the token is valid.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param filterChain the filter chain to continue processing
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI();

        String jwt = extractJwtFromCookies(request);
        String username = null;

        if (jwt != null) {
            try {
                username = jwtUtil.extractUsername(jwt);
                logger.debug("JWT found in cookie for user `{}`", username);
            } catch (Exception e) {
                logger.warn("Unable to extract username from JWT (cookie): {}", e.getMessage());
            }
        } else {
            logger.debug("No '{}' cookie found for request {}", JWT_COOKIE_NAME, requestURI);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            if (jwtUtil.validateToken(jwt)) {
                logger.info("Valid JWT for `{}`", username);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                logger.debug("Authentication configured for `{}`", username);
            } else {
                logger.warn("Invalid or expired JWT for `{}`", username);
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Extracts JWT token from request cookies.
     *
     * @param request the HTTP servlet request
     * @return the JWT token if found, null otherwise
     */
    private String extractJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null)
            return null;

        Optional<Cookie> jwtCookie = java.util.Arrays.stream(cookies)
                .filter(c -> JWT_COOKIE_NAME.equals(c.getName()))
                .findFirst();
        return jwtCookie.map(Cookie::getValue).orElse(null);
    }

    /**
     * Determines if the filter should not be applied to certain paths.
     *
     * @param request the HTTP servlet request
     * @return true if the filter should be skipped, false otherwise
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/css/") ||
                path.startsWith("/actuator/") ||
                path.equals("/login") ||
                path.equals("/error");
    }
}
