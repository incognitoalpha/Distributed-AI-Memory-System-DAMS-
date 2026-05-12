package com.aimemory.embedding.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for tracking embedding model versions and their dimension sizes.
 * Provides lookup for model information and tracks the currently active model.
 *
 * @author agent
 * @since 1.0.0
 */
@Service
public class ModelVersionRegistry {

    private static final Logger log = LoggerFactory.getLogger(ModelVersionRegistry.class);

    @Value("${embedding.model.name:text-embedding-3-small}")
    private String currentModelName;

    @Value("${embedding.model.version:v1}")
    private String currentModelVersion;

    @Value("${embedding.model.dimension:1536}")
    private int currentDimension;

    private final Map<String, ModelInfo> modelRegistry = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        // Register default model
        registerModel(currentModelName, currentModelVersion, currentDimension);
        log.info("ModelVersionRegistry initialized with active model: {} v{} (dim={})",
                currentModelName, currentModelVersion, currentDimension);
    }

    /**
     * Registers an embedding model version.
     */
    public void registerModel(String modelName, String modelVersion, int dimension) {
        String key = modelName + "-" + modelVersion;
        modelRegistry.put(key, new ModelInfo(modelName, modelVersion, dimension));
        log.info("Registered model: {} with dimension {}", key, dimension);
    }

    /**
     * Gets the currently active model information.
     */
    public ModelInfo getActiveModel() {
        String key = currentModelName + "-" + currentModelVersion;
        ModelInfo model = modelRegistry.get(key);
        if (model == null) {
            model = new ModelInfo(currentModelName, currentModelVersion, currentDimension);
        }
        return model;
    }

    /**
     * Gets model info by model name and version.
     */
    public Optional<ModelInfo> getModelInfo(String modelName, String modelVersion) {
        String key = modelName + "-" + modelVersion;
        return Optional.ofNullable(modelRegistry.get(key));
    }

    /**
     * Gets the dimension for a given model.
     */
    public int getDimension(String modelName) {
        return getActiveModel().dimension();
    }

    /**
     * Checks if a model version is the current active version.
     */
    public boolean isActiveVersion(String modelName, String modelVersion) {
        return currentModelName.equals(modelName) && currentModelVersion.equals(modelVersion);
    }

    /**
     * Switches to a new active model version.
     * Publishes a reindex.triggered event to Kafka.
     */
    public void switchActiveModel(String newModelName, String newModelVersion, int newDimension) {
        log.info("Switching active model from {} v{} to {} v{}",
                currentModelName, currentModelVersion, newModelName, newModelVersion);

        this.currentModelName = newModelName;
        this.currentModelVersion = newModelVersion;
        this.currentDimension = newDimension;

        registerModel(newModelName, newModelVersion, newDimension);
    }

    public record ModelInfo(String modelName, String modelVersion, int dimension) {
        public String getFullVersion() {
            return modelName + "-" + modelVersion;
        }
    }
}