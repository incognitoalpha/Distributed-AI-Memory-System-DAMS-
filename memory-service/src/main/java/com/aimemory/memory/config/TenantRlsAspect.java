package com.aimemory.memory.config;

import com.aimemory.shared.domain.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Statement;

/**
 * Aspect to enforce Row-Level Security (RLS) by setting the PostgreSQL session
 * variable before executing queries in repositories.
 * 
 * @author agent
 * @since 1.0.0
 */
@Aspect
@Component
public class TenantRlsAspect {

    private static final Logger log = LoggerFactory.getLogger(TenantRlsAspect.class);

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Intercepts any method in the repository package and executes the SET LOCAL command.
     * This ensures that RLS is applied to every query, including inherited methods
     * like save(), findAll(), and findById().
     */
    @Before("execution(* com.aimemory.memory.repository..*.*(..))")
    public void setTenantContext() {
        if (TenantContext.isSet()) {
            String tenantId = TenantContext.getTenantId().toString();
            System.out.println("DEBUG: Aspect triggering for tenant: " + tenantId);
            log.info("Enforcing RLS tenant context for repository: tenantId={}", tenantId);
            
            entityManager.createNativeQuery("SET LOCAL app.current_tenant_id = '" + tenantId + "'")
                         .executeUpdate();
        } else {
            System.out.println("DEBUG: Aspect triggering with NO tenant context.");
            log.debug("No tenant context set, resetting RLS session variable to nil UUID.");
            // Use the nil UUID to avoid casting errors while still ensuring no data is matched
            entityManager.createNativeQuery("SET LOCAL app.current_tenant_id = '00000000-0000-0000-0000-000000000000'")
                         .executeUpdate();
        }
    }
}
