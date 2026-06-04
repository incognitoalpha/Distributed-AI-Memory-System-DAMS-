package com.aimemory.shared.exception;

/**
 * Exception thrown when the retrieval pipeline fails unexpectedly.
 *
 * @author agent
 * @since 1.0.0
 */
public final class RetrievalPipelineException extends AiMemoryException {

    public RetrievalPipelineException(String message) {
        super("RETRIEVAL_PIPELINE_ERROR", message);
    }

    public RetrievalPipelineException(String message, Throwable cause) {
        super("RETRIEVAL_PIPELINE_ERROR", message, cause);
    }
}
