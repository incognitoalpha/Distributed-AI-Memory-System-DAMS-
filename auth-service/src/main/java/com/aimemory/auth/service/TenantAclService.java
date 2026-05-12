package com.aimemory.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service for memory-level Access Control List (ACL) checks.
 * Determines if a principal can access a specific memory resource.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class TenantAclService {

    private static final Logger log = LoggerFactory.getLogger(TenantAclService.class);

    /**
     * Checks if the given principal can access the memory belonging to the specified tenant.
     * Access is granted only if the principal's tenant matches the resource's tenant.
     *
     * @param principalTenantId the tenant ID from the principal's JWT
     * @param resourceTenantId the tenant ID of the resource (memory)
     * @return true if access is allowed, false otherwise
     */
    public boolean canAccessMemory(UUID principalTenantId, UUID resourceTenantId) {
        if (principalTenantId == null || resourceTenantId == null) {
            log.warn("ACL check failed: null tenant ID provided");
            return false;
        }

        boolean allowed = principalTenantId.equals(resourceTenantId);

        if (!allowed) {
            log.warn("ACL denied: principal tenant {} does not match resource tenant {}",
                    principalTenantId, resourceTenantId);
        }

        return allowed;
    }

    /**
     * Checks if the given principal can access memories for the specified user.
     * Users can access their own memories; agents can access any user within their tenant.
     *
     * @param principalTenantId the tenant ID from the principal's JWT
     * @param principalUserId  the user ID from the principal's JWT
     * @param principalRoles  the roles of the principal
     * @param targetUserId     the user ID whose memories are being accessed
     * @return true if access is allowed, false otherwise
     */
    public boolean canAccessUserMemories(
            UUID principalTenantId,
            UUID principalUserId,
            List<String> principalRoles,
            UUID targetUserId) {

        if (principalTenantId == null || targetUserId == null) {
            return false;
        }

        // User can access their own memories
        if (principalUserId.equals(targetUserId)) {
            return true;
        }

        // Agent can access any user within their tenant
        if (principalRoles != null && principalRoles.contains("ROLE_AGENT")) {
            return true;
        }

        log.warn("ACL denied: user {} cannot access memories for user {}",
                principalUserId, targetUserId);
        return false;
    }

    /**
     * Checks if the principal has the required role.
     *
     * @param principalRoles the roles from the principal's JWT
     * @param requiredRole  the required role (e.g., "ROLE_AGENT", "ROLE_USER")
     * @return true if the principal has the required role
     */
    public boolean hasRole(List<String> principalRoles, String requiredRole) {
        if (principalRoles == null || requiredRole == null) {
            return false;
        }
        return principalRoles.contains(requiredRole);
    }
}