package com.aimemory.memory.config;

import com.aimemory.shared.domain.TenantContext;
import jakarta.persistence.EntityManager;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Pure Spring AOP Method Interceptor to enforce Row-Level Security (RLS).
 * Intercepts repository calls and executes 'SET LOCAL app.current_tenant_id'.
 * 
 * @author agent
 * @since 1.0.0
 */
public class TenantRlsInterceptor implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TenantRlsInterceptor.class);
    
    private final EntityManager entityManager;

    public TenantRlsInterceptor(EntityManager entityManager) {
        this.entityManager = entityManager;
        System.out.println("DEBUG: TenantRlsInterceptor initialized!");
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        if (TenantContext.isSet()) {
            String tenantId = TenantContext.getTenantId().toString();
            System.out.println("DEBUG: Interceptor triggering for tenant: " + tenantId);
            log.info("Enforcing RLS tenant context for repository: tenantId={}", tenantId);
            
            entityManager.createNativeQuery("SET LOCAL app.current_tenant_id = '" + tenantId + "'")
                         .executeUpdate();
        } else {
            System.out.println("DEBUG: Interceptor triggering with NO tenant context.");
            // Use the nil UUID to avoid casting errors
            entityManager.createNativeQuery("SET LOCAL app.current_tenant_id = '00000000-0000-0000-0000-000000000000'")
                         .executeUpdate();
        }
        
        return invocation.proceed();
    }
}
