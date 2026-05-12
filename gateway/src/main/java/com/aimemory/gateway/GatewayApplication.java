package com.aimemory.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;

/**
 * API Gateway - entry point for all client requests.
 * Handles JWT validation, tenant injection, and rate limiting.
 *
 * @author agent
 * @since 1.0.0
 */
@SpringBootApplication(
    scanBasePackages = "com.aimemory",
    exclude = {
        DataSourceAutoConfiguration.class, 
        HibernateJpaAutoConfiguration.class,
        // ReactiveSecurityAutoConfiguration.class, // We want to keep this but use our config
        ReactiveUserDetailsServiceAutoConfiguration.class
    }
)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}