package com.medilabo.solutions.front.config;

import java.util.Collections;

import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign {@link RequestInterceptor} that forwards the JWT cookie from the
 * incoming HTTP request
 * to outgoing Feign client requests.
 *
 * <p>
 * This interceptor is used to propagate the "jwt" cookie (typically containing
 * a JWT token)
 * so that downstream services behind the API Gateway can perform authentication
 * or authorization
 * without needing re-authentication.
 * </p>
 *
 * <p>
 * It accesses the current HTTP request using Spring's
 * {@link RequestContextHolder}, extracts the
 * "jwt" cookie, and attaches it to the Feign request's "Cookie" header.
 * </p>
 *
 * <p>
 * If the "Cookie" header already exists, the JWT is appended using a semicolon
 * separator.
 * </p>
 */
@Component
@Slf4j
public class JwtCookieFeignInterceptor implements RequestInterceptor {

    private static final String COOKIE_NAME = "jwt";

    /**
     * Intercepts and modifies the outgoing Feign request by adding the JWT cookie,
     * if it is present in the current HTTP request.
     *
     * @param template the {@link RequestTemplate} used to build the Feign request
     */
    @Override
    public void apply(RequestTemplate template) {

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attrs == null)
            return;

        HttpServletRequest request = attrs.getRequest();
        if (request == null || request.getCookies() == null)
            return;

        for (Cookie c : request.getCookies()) {
            if (COOKIE_NAME.equals(c.getName())) {

                String newCookie = COOKIE_NAME + '=' + c.getValue();

                if (template.headers().containsKey("Cookie")) {
                    String existing = template.headers()
                            .getOrDefault("Cookie", Collections.emptyList())
                            .stream().findFirst().orElse("");
                    template.header("Cookie", existing + "; " + newCookie);
                } else {
                    template.header("Cookie", newCookie);
                }
                break;
            }
        }
    }
}
