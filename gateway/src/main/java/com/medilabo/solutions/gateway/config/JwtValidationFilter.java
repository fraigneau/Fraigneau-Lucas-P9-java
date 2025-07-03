package com.medilabo.solutions.gateway.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.medilabo.solutions.gateway.util.JwtValidatorUtil;

import reactor.core.publisher.Mono;

/**
 * Reactive WebFilter that intercepts incoming HTTP requests to validate JWT
 * tokens.
 * 
 * <p>
 * This filter performs the following tasks:
 * <ul>
 * <li>Extracts the JWT token from the "jwt" cookie</li>
 * <li>Validates the token using {@link JwtValidatorUtil}</li>
 * <li>Checks that the request originates from the frontend using a custom
 * header</li>
 * <li>If valid, sets the security context with a {@code ROLE_USER}</li>
 * <li>If invalid, redirects the user to the login page</li>
 * </ul>
 * </p>
 * 
 * <p>
 * This filter is typically added before the authentication step in the Spring
 * Security filter chain.
 * </p>
 */
@Component
public class JwtValidationFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtValidationFilter.class);

    private static final String INTERNAL_HEADER = "X-Internal-Front";

    @Autowired
    private JwtValidatorUtil jwtValidator;

    /**
     * Main filter logic that processes incoming requests.
     *
     * @param exchange the current {@link ServerWebExchange}
     * @param chain    the {@link WebFilterChain} to delegate to the next filter
     * @return a {@link Mono} that completes when the filter chain has been
     *         processed
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getPath();

        String jwt = extractJwtFromCookies(req);
        if (jwt != null) {

            if (jwtValidator.isValid(jwt) && isFromFront(req)) {
                log.info("Valid JWT signature for request: {}", path);

                String username = jwtValidator.extractClaim(jwt, "sub");

                var auth = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_USER")));

                var ctx = new SecurityContextImpl(auth);

                return chain.filter(exchange)
                        .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(ctx)));
            }

            log.warn("Invalid JWT signature for request: {}", path);
            return redirect(exchange, "http://localhost:8084/login");
        }

        log.warn("No JWT token found in cookies for request: {}", path);
        return chain.filter(exchange);
    }

    /**
     * Extracts the JWT token from cookies.
     *
     * @param req the current {@link ServerHttpRequest}
     * @return the JWT token string if present, or {@code null} if not found
     */
    private String extractJwtFromCookies(ServerHttpRequest req) {
        return req.getCookies().getFirst("jwt") != null
                ? req.getCookies().getFirst("jwt").getValue()
                : null;
    }

    /**
     * Verifies whether the request originated from the frontend.
     *
     * @param req the current {@link ServerHttpRequest}
     * @return {@code true} if the internal header is present, {@code false}
     *         otherwise
     */
    private boolean isFromFront(ServerHttpRequest req) {
        return req.getHeaders().containsKey(INTERNAL_HEADER);
    }

    /**
     * Redirects the user to a specified URL using HTTP 303 SEE_OTHER.
     *
     * @param exchange the current {@link ServerWebExchange}
     * @param url      the target URL to redirect to
     * @return a {@link Mono} that completes when the response is finished
     */
    private Mono<Void> redirect(ServerWebExchange exchange, String url) {
        exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
        exchange.getResponse().getHeaders().set(HttpHeaders.LOCATION, url);
        return exchange.getResponse().setComplete();
    }
}
