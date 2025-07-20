package com.talentradar.assessment_service.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Add this log to verify the filter is running
        log.info("UserContextFilter is executing for path: {}", request.getRequestURI());

        // Log all headers to debug
        log.info("All request headers:");
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            String headerValue = request.getHeader(headerName);
            log.info("Header: {} = {}", headerName, headerValue);
        }

        String userId = request.getHeader("X-User-Id");
        String userEmail = request.getHeader("X-User-Email");
        String userRoles = request.getHeader("X-User-Role");
        String userName = request.getHeader("X-User-UserName");
        String fullName = request.getHeader("X-User-FullName");

        log.info("Extracted headers - UserId: {}, UserEmail: {}, UserRoles: {}, UserName: {}, FullName: {}",
                userId, userEmail, userRoles, userName, fullName);

        if (userId != null && !userId.trim().isEmpty() &&
                userRoles != null && !userRoles.trim().isEmpty()) {

            List<SimpleGrantedAuthority> authorities = Arrays.stream(userRoles.split(","))
                    .map(String::trim)
                    .filter(role -> !role.isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.info("User authenticated successfully: {} with roles: {}", userId, authorities);
        } else {
            log.warn("Authentication failed - Missing or empty userId ({}) or userRoles ({})", userId, userRoles);
        }

        filterChain.doFilter(request, response);
        log.info("UserContextFilter completed for path: {}", request.getRequestURI());
    }
}