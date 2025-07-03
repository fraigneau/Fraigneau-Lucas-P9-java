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
import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuration de sécurité Spring Security avec support JWT.
 * 
 * Cette configuration :
 * - Désactive la gestion de session (stateless)
 * - Configure l'authentification JWT
 * - Définit les règles d'autorisation pour les endpoints
 * - Intègre le filtre JWT personnalisé
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
     * Configuration principale de la chaîne de filtres de sécurité.
     * 
     * @param http l'objet HttpSecurity pour configurer la sécurité
     * @return SecurityFilterChain la chaîne de filtres configurée
     * @throws Exception en cas d'erreur de configuration
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
     * Encoder de mot de passe BCrypt.
     * 
     * @return PasswordEncoder l'encoder BCrypt configuré
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Service de détails utilisateur en mémoire pour les tests.
     * En production, vous devriez remplacer ceci par une implémentation
     * qui récupère les utilisateurs depuis une base de données.
     * 
     * @return UserDetailsService le service de détails utilisateur
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
     * Gestionnaire d'authentification.
     * 
     * @param authConfig la configuration d'authentification
     * @return AuthenticationManager le gestionnaire d'authentification
     * @throws Exception en cas d'erreur de configuration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}