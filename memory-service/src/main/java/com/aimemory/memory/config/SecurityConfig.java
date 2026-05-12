package com.aimemory.memory.config;

import com.aimemory.shared.domain.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Security configuration for memory-service.
 * Extracts tenant and user from X-Tenant-Id and X-User-Id headers (set by Gateway).
 *
 * @author agent
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(tenantContextFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public OncePerRequestFilter tenantContextFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain) throws ServletException, IOException {
                try {
                    String tenantIdHeader = request.getHeader("X-Tenant-Id");
                    String userIdHeader = request.getHeader("X-User-Id");
                    String rolesHeader = request.getHeader("X-User-Roles");

                    if (tenantIdHeader != null && userIdHeader != null) {
                        try {
                            UUID tenantId = UUID.fromString(tenantIdHeader);
                            UUID userId = UUID.fromString(userIdHeader);
                            TenantContext.set(tenantId, userId);
                            
                            // Set Spring Security Authentication
                            List<SimpleGrantedAuthority> authorities = List.of();
                            if (rolesHeader != null && !rolesHeader.isEmpty()) {
                                authorities = Arrays.stream(rolesHeader.split(","))
                                        .map(SimpleGrantedAuthority::new)
                                        .collect(Collectors.toList());
                            }
                            
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    userId, null, authorities);
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            
                            log.debug("Tenant context and authentication set: tenantId={} userId={} roles={}", 
                                    tenantId, userId, rolesHeader);
                        } catch (IllegalArgumentException e) {
                            log.warn("Invalid tenant/user ID in headers: tenantId={} userId={}",
                                    tenantIdHeader, userIdHeader);
                        }
                    }

                    filterChain.doFilter(request, response);
                } finally {
                    TenantContext.clear();
                }
            }
        };
    }
}