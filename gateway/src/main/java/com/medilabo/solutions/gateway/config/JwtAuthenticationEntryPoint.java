package com.medilabo.solutions.gateway.config;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

/**
 * Custom entry point used by Spring Security for handling unauthorized access
 * (HTTP 401).
 *
 * <p>
 * This component differentiates between frontend and API requests:
 * <ul>
 * <li>For API requests (URLs containing "/api"), it redirects the user to the
 * homepage.</li>
 * <li>For non-API requests, it directly returns a 401 Unauthorized
 * response.</li>
 * </ul>
 * </p>
 *
 * <p>
 * This logic helps integrate frontend redirection with a stateless JWT-based
 * backend.
 * </p>
 */
@Component
public class JwtAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    /**
     * Determines whether the request is targeting an API endpoint.
     *
     * @param req the {@link ServerHttpRequest} to inspect
     * @return {@code true} if the request path contains "/api", {@code false}
     *         otherwise
     */
    private boolean isApiRequest(ServerHttpRequest req) {
        return req.getPath().value().contains("/api");
    }

    /**
     * Handles unauthorized access attempts.
     *
     * <p>
     * If the request is for an API route, redirects to the homepage.
     * Otherwise, responds with HTTP 401 Unauthorized.
     * </p>
     *
     * @param exchange      the {@link ServerWebExchange} representing the current
     *                      request/response
     * @param authException the exception that triggered the entry point
     * @return a {@link Mono} that completes when the response is committed
     */
    @Override
    public Mono<Void> commence(ServerWebExchange exchange,
            AuthenticationException authException) {

        ServerHttpRequest req = exchange.getRequest();
        String path = req.getURI().getPath();
        HttpMethod method = req.getMethod();

        log.warn("Unauthorized access to {} {} - {}", method, path, authException.getMessage());

        if (!isApiRequest(req)) {
            log.warn("Unauthorized access to non-API endpoint: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        log.info("Redirecting to home page for unauthorized access from front: {}", path);
        return redirect(exchange, "http://localhost:8084/home");
    }

    /**
     * Sends an HTTP redirect (303 See Other) to the specified URL.
     *
     * @param exchange the {@link ServerWebExchange}
     * @param url      the redirection target
     * @return a {@link Mono} indicating completion of the response
     */
    private Mono<Void> redirect(ServerWebExchange exchange, String url) {
        exchange.getResponse().setStatusCode(HttpStatus.SEE_OTHER);
        exchange.getResponse().getHeaders().set(HttpHeaders.LOCATION, url);
        return exchange.getResponse().setComplete();
    }
}
