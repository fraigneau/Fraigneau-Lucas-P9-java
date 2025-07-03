package com.medilabo.solutions.front.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import jakarta.servlet.http.Cookie;

/**
 * Security configuration class for the Spring Security framework.
 * 
 * This class configures the security settings for the web application
 * including:
 * - JWT authentication filter integration
 * - Request authorization rules
 * - Exception handling for authentication failures
 * - Logout functionality with JWT cookie management
 * - Password encoding using BCrypt
 * - In-memory user details service for development/testing
 * - Authentication manager configuration
 * 
 * The configuration disables CSRF protection as JWT tokens are used for
 * authentication.
 * Public endpoints like login, logout, CSS resources, actuator endpoints, and
 * error pages
 * are accessible without authentication, while all other requests require
 * authentication.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    SecurityContextLogoutHandler securityContextLogoutHandler = new SecurityContextLogoutHandler();

    /**
     * Main configuration of the security filter chain.
     * 
     * @param http the HttpSecurity object to configure security
     * @return SecurityFilterChain the configured filter chain
     * @throws Exception in case of configuration error
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/login", "/logout", "/css/**", "/actuator/**", "/error").permitAll()
                        .anyRequest().authenticated())

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint))

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            Cookie cookie = new Cookie("jwt", "");
                            cookie.setHttpOnly(true);
                            cookie.setSecure(false);
                            cookie.setPath("/");
                            cookie.setMaxAge(0);
                            response.addCookie(cookie);

                            response.sendRedirect("/login?logout");
                        }));

        return http.build();
    }

    /**
     * BCrypt password encoder.
     * 
     * @return PasswordEncoder the configured BCrypt encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * In-memory user details service for testing.
     * In production, you should replace this with an implementation
     * that retrieves users from a database.
     * 
     * @return UserDetailsService the user details service
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
                .username("admin")
                .password(passwordEncoder().encode("123"))
                .roles("USER", "ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    /**
     * Authentication manager.
     * 
     * @param authConfig the authentication configuration
     * @return AuthenticationManager the authentication manager
     * @throws Exception in case of configuration error
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}