package com.aimemory.shared.domain;

import java.util.UUID;

/**
 * Thread-local holder for tenant context used in Row-Level Security (RLS) enforcement.
 * The tenantId is sourced from the JWT and should never be taken from request bodies.
 *
 * @author agent
 * @since 1.0.0
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<UUID> CURRENT_USER = new ThreadLocal<>();

    private TenantContext() {
        // Utility class - no instantiation
    }

    /**
     * Sets the current tenant context for the calling thread.
     *
     * @param tenantId the tenant identifier
     * @param userId   the user identifier
     */
    public static void set(UUID tenantId, UUID userId) {
        CURRENT_TENANT.set(tenantId);
        CURRENT_USER.set(userId);
    }

    /**
     * Gets the current tenant ID from the thread-local context.
     *
     * @return the current tenant ID, or null if not set
     */
    public static UUID getTenantId() {
        return CURRENT_TENANT.get();
    }

    /**
     * Gets the current user ID from the thread-local context.
     *
     * @return the current user ID, or null if not set
     */
    public static UUID getUserId() {
        return CURRENT_USER.get();
    }

    /**
     * Clears the tenant context from the current thread.
     * Must be called after request processing to prevent context leakage.
     */
    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_USER.remove();
    }

    /**
     * Checks if tenant context is currently set.
     *
     * @return true if both tenant and user context are set
     */
    public static boolean isSet() {
        return CURRENT_TENANT.get() != null && CURRENT_USER.get() != null;
    }
}