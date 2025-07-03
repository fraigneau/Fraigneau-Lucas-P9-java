package com.medilabo.solutions.gateway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;

/**
 * Configuration class for securing the reactive Spring Cloud Gateway
 * application.
 * 
 * <p>
 * This class defines the security filter chain used to:
 * <ul>
 * <li>Disable CSRF protection and basic/form login authentication
 * mechanisms</li>
 * <li>Restrict access to the "/api/**" endpoints to authenticated users</li>
 * <li>Allow unrestricted access to actuator endpoints ("/actuator/**")</li>
 * <li>Apply a custom JWT validation filter for authentication</li>
 * <li>Handle unauthorized and access-denied requests gracefully</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Autowired
    private ServerAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtValidationFilter jwtValidationFilter;

    /**
     * Defines the reactive security filter chain for the application.
     *
     * @param http the {@link ServerHttpSecurity} to configure
     * @return the configured {@link SecurityWebFilterChain}
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        ServerAccessDeniedHandler accessDeniedRedirect = (ServerAccessDeniedHandler) (exchange, denied) -> {
            exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
            exchange.getResponse().getHeaders()
                    .set(HttpHeaders.LOCATION, "http://localhost:8084/home");
            return exchange.getResponse().setComplete();
        };

        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/api/**").authenticated())
                .exceptionHandling(exceptionHandlingSpec -> exceptionHandlingSpec
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedRedirect))
                .addFilterAt(jwtValidationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }
}
