package com.aimemory.memory.domain.enums;

/**
 * Strategies for resolving memory conflicts when newer memories contradict older ones.
 *
 * @author agent
 * @since 1.0.0
 */
public enum ConflictResolutionStrategy {
    /**
     * Latest memory wins - the newer version replaces the older one.
     */
    LATEST_WINS,

    /**
     * Merge content - combine both versions into a unified memory.
     */
    MERGE,

    /**
     * Manual resolution required - flag for human review.
     */
    MANUAL
}