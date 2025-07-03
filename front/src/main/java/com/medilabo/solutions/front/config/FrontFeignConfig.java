package com.medilabo.solutions.front.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.RequestInterceptor;

/**
 * Configuration class for customizing Feign client requests in the frontend
 * service.
 *
 * <p>
 * This configuration adds a custom HTTP header {@code X-Internal-Front: true}
 * to every
 * outgoing Feign request. This allows backend services (such as the API
 * gateway) to identify
 * and validate that the request originates from a trusted frontend component.
 * </p>
 *
 * <p>
 * This is typically used to authorize JWT tokens only when the frontend is the
 * source of the call.
 * </p>
 */
@Configuration
public class FrontFeignConfig {

    /**
     * Defines a {@link RequestInterceptor} that adds the "X-Internal-Front" header
     * to all Feign requests.
     *
     * @return the configured {@link RequestInterceptor}
     */
    @Bean
    public RequestInterceptor frontInternalHeader() {
        return template -> template.header("X-Internal-Front", "true");
    }
}
