package com.aimemory.memory.config;

import jakarta.persistence.EntityManager;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration to register the RLS Advisor.
 * Directly targets any class that is a Spring Data Repository.
 * 
 * @author agent
 * @since 1.0.0
 */
@Configuration
public class TenantRlsAdvisorConfig {

    @Bean
    public Advisor tenantRlsAdvisor(EntityManager entityManager) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        // Target any method on any Repository
        pointcut.setExpression("this(org.springframework.data.repository.Repository)");
        
        System.out.println("DEBUG: Registering TenantRlsAdvisor targeting repositories.");
        return new DefaultPointcutAdvisor(pointcut, new TenantRlsInterceptor(entityManager));
    }
}
