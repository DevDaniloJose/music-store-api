package com.vadastore.music_store_api.security;

import com.vadastore.music_store_api.service.UserDetailsServiceImpl;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

@AllArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtility jwtUtility;
    private final HandlerExceptionResolver handlerExceptionResolver;
    private final UserDetailsServiceImpl userDetailsService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            processToken(request);
        } catch (Exception e) {
            logger.error("Failed to process JWT Token: {}", e.getMessage());
            handlerExceptionResolver.resolveException(request, response, null, e);
        }

        logger.debug("Processing complete. Return back control to framework");
        filterChain.doFilter(request, response);
    }

    private void processToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        logger.info("Authorization Header received");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.info("No bearer header, skip processing");
            return;
        }

        final String jwtToken = authHeader.substring(7);

        try {
            Claims claims = jwtUtility.extractAllClaims(jwtToken);
            String username = claims.getSubject();

            if (username == null) {
                logger.info("No username found in JWT Token");
                return;
            }

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                logger.info("Create authentication instance for {}", username);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

        } catch (JwtException e) {
            logger.warn("Invalid or expired JWT token: {}", e.getMessage());
        }
    }
}