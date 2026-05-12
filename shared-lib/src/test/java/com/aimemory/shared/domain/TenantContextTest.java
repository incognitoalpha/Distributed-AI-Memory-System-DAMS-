package com.aimemory.shared.domain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    private UUID testTenantId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void set_and_get_tenant_id() {
        TenantContext.set(testTenantId, testUserId);
        assertEquals(testTenantId, TenantContext.getTenantId());
    }

    @Test
    void set_and_get_user_id() {
        TenantContext.set(testTenantId, testUserId);
        assertEquals(testUserId, TenantContext.getUserId());
    }

    @Test
    void clear_removes_context() {
        TenantContext.set(testTenantId, testUserId);
        TenantContext.clear();
        assertNull(TenantContext.getTenantId());
        assertNull(TenantContext.getUserId());
    }

    @Test
    void is_set_returns_false_when_not_set() {
        assertFalse(TenantContext.isSet());
    }

    @Test
    void is_set_returns_true_when_set() {
        TenantContext.set(testTenantId, testUserId);
        assertTrue(TenantContext.isSet());
    }

    @Test
    void get_tenant_id_returns_null_when_not_set() {
        assertNull(TenantContext.getTenantId());
    }

    @Test
    void get_user_id_returns_null_when_not_set() {
        assertNull(TenantContext.getUserId());
    }

    @Test
    void context_is_thread_local() throws InterruptedException {
        TenantContext.set(testTenantId, testUserId);

        Thread otherThread = new Thread(() -> {
            // Other thread should not see the context
            assertNull(TenantContext.getTenantId());
            assertNull(TenantContext.getUserId());
        });

        otherThread.start();
        otherThread.join();

        // Main thread should still have the context
        assertEquals(testTenantId, TenantContext.getTenantId());
    }

    @Test
    void set_overwrites_previous_context() {
        UUID newTenantId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();

        TenantContext.set(testTenantId, testUserId);
        TenantContext.set(newTenantId, newUserId);

        assertEquals(newTenantId, TenantContext.getTenantId());
        assertEquals(newUserId, TenantContext.getUserId());
    }
}