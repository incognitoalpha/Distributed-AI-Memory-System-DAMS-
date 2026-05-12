package com.aimemory.embedding.api;

import com.aimemory.embedding.service.ModelVersionRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for embedding model operations.
 *
 * @author agent
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/embedding-models")
public class EmbeddingController {

    private final ModelVersionRegistry modelRegistry;

    public EmbeddingController(ModelVersionRegistry modelRegistry) {
        this.modelRegistry = modelRegistry;
    }

    /**
     * Returns the currently active embedding model.
     */
    @GetMapping("/current")
    public ResponseEntity<Map<String, Object>> getCurrentModel() {
        ModelVersionRegistry.ModelInfo model = modelRegistry.getActiveModel();

        return ResponseEntity.ok(Map.of(
                "modelName", model.modelName(),
                "modelVersion", model.modelVersion(),
                "dimension", model.dimension(),
                "isActive", true
        ));
    }

    /**
     * Health check endpoint.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }
}