package com.aimemory.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Utility class for JSON serialization/deserialization.
 * Provides a singleton ObjectMapper with proper configuration.
 *
 * @author agent
 * @since 1.0.0
 */
public final class JsonUtils {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new JavaTimeModule());
        MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    private JsonUtils() {
        // Utility class - no instantiation
    }

    /**
     * Returns the configured ObjectMapper instance.
     *
     * @return configured ObjectMapper
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    /**
     * Serializes an object to JSON string.
     *
     * @param object the object to serialize
     * @return JSON string
     * @throws JsonProcessingException if serialization fails
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return MAPPER.writeValueAsString(object);
    }

    /**
     * Deserializes JSON string to object.
     *
     * @param json   the JSON string
     * @param clazz  the target class
     * @param <T>    the target type
     * @return deserialized object
     * @throws JsonProcessingException if deserialization fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) throws JsonProcessingException {
        return MAPPER.readValue(json, clazz);
    }

    /**
     * Serializes an object to pretty-printed JSON string.
     *
     * @param object the object to serialize
     * @return pretty-printed JSON string
     * @throws JsonProcessingException if serialization fails
     */
    public static String toPrettyJson(Object object) throws JsonProcessingException {
        return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
    }
}