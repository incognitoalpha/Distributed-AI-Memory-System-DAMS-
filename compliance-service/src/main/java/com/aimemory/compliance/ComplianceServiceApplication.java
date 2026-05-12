package com.aimemory.compliance;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Compliance Service - handles GDPR erasure and audit logging.
 *
 * @author agent
 * @since 1.0.0
 */
@SpringBootApplication
public class ComplianceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComplianceServiceApplication.class, args);
    }
}