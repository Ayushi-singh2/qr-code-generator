package com.example.qrgenerator.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Spring Security 6's default CsrfTokenRequestHandler resolves the token lazily,
 * so CookieCsrfTokenRepository never actually writes the XSRF-TOKEN cookie unless
 * something reads the token on the request. Server-rendered apps get this for free
 * via ${_csrf.token} in a template; a pure REST/SPA backend does not.
 *
 * This filter forces that resolution on every request, so the cookie is reliably
 * present for the frontend (axios) to read and echo back as X-XSRF-TOKEN.
 */
public class CsrfCookieFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute("_csrf");
        if (csrfToken != null) {
            // Touching .getToken() forces the deferred supplier to resolve and save the token,
            // which is what actually triggers CookieCsrfTokenRepository to write the cookie.
            csrfToken.getToken();
        }
        filterChain.doFilter(request, response);
    }
}