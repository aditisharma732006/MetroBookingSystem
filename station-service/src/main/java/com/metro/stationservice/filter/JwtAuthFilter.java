package com.metro.stationservice.filter;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.metro.stationservice.util.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Reads (GET, HEAD, OPTIONS) are open: no token needed.
 * Writes (POST, PUT, PATCH, DELETE) need a valid JWT with role ADMIN.
 *
 * Booking Service calls GET /route through Feign WITHOUT a token.
 * Do not require authentication on GETs Booking uses unless Booking
 * also forwards the token (a Feign RequestInterceptor).
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String method = request.getMethod();

        // Reads are open
        if ("GET".equals(method) || "HEAD".equals(method) || "OPTIONS".equals(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Writes need a valid token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Missing or malformed Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid or expired token");
            return;
        }

        // ... and the ADMIN role
        Claims claims = jwtUtil.extractClaims(token);
        String role = claims.get("role", String.class);
        if (!"ADMIN".equals(role)) {
            writeError(response, HttpServletResponse.SC_FORBIDDEN, "Admin access required");
            return;
        }

        request.setAttribute("role", role);
        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}