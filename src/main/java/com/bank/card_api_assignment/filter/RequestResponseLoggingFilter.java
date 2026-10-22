package com.bank.card_api_assignment.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger("API_LOGGER");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");  
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(req, 1024 * 1024);
        ContentCachingResponseWrapper response = new ContentCachingResponseWrapper(res);
        long start = System.currentTimeMillis();

        try {
            chain.doFilter(request, response);
        } finally {
            long took = System.currentTimeMillis() - start;
            String query = request.getQueryString() == null ? "" : "?" + request.getQueryString();
            String reqBody = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
            String resBody = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);

            log.info("REQUEST  | {} {}{} | body={}", request.getMethod(), request.getRequestURI(), query, reqBody);
            log.info("RESPONSE | {} {} | status={} | {}ms | body={}",
                    request.getMethod(), request.getRequestURI(), response.getStatus(), took, resBody);

            response.copyBodyToResponse();
        }
    }
}
