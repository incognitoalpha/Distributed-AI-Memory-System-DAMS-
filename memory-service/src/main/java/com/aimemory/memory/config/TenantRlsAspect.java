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
     * This ensures that RLS is applied to every query executed within a repository.
     */
    @Before("execution(* com.aimemory.memory.repository.*.*(..))")
    public void setTenantContext() {
        if (TenantContext.isSet()) {
            String tenantId = TenantContext.getTenantId().toString();
            log.trace("Enforcing RLS tenant context for repository: tenantId={}", tenantId);
            
            // Execute SET LOCAL on the current connection using Hibernate's doWork.
            // Executing this as a separate statement ensures Hibernate's row count
            // checks for inserts/updates remain accurate.
            Session session = entityManager.unwrap(Session.class);
            session.doWork(connection -> {
                try (Statement statement = connection.createStatement()) {
                    statement.execute("SET LOCAL app.current_tenant_id = '" + tenantId + "'");
                }
            });
        } else {
            log.warn("Executing repository method without tenant context! If RLS is forced, this may fail.");
        }
    }
}
