package com.aimemory.shared.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void toJson_serializes_object() throws JsonProcessingException {
        String json = JsonUtils.toJson(Map.of("key", "value"));
        assertTrue(json.contains("key"));
        assertTrue(json.contains("value"));
    }

    @Test
    void fromJson_deserializes_object() throws JsonProcessingException {
        String json = "{\"name\":\"test\",\"count\":42}";
        Map<String, Object> result = JsonUtils.fromJson(json, Map.class);

        assertEquals("test", result.get("name"));
        assertEquals(42, result.get("count"));
    }

    @Test
    void toPrettyJson_prints_formatted() throws JsonProcessingException {
        Map<String, Object> obj = Map.of("name", "test", "count", 42);
        String pretty = JsonUtils.toPrettyJson(obj);

        assertTrue(pretty.contains("name"));
        assertTrue(pretty.contains("test"));
        assertTrue(pretty.contains("\n")); // Pretty print has newlines
    }

    @Test
    void toJson_handles_null() throws JsonProcessingException {
        String json = JsonUtils.toJson(null);
        assertEquals("null", json);
    }

    @Test
    void fromJson_handles_empty_object() throws JsonProcessingException {
        Map<String, Object> result = JsonUtils.fromJson("{}", Map.class);
        assertTrue(result.isEmpty());
    }

    @Test
    void toJson_handles_complex_object() throws JsonProcessingException {
        TestObject obj = new TestObject("test", 42, true);
        String json = JsonUtils.toJson(obj);

        assertTrue(json.contains("test"));
        assertTrue(json.contains("42"));
    }

    @Test
    void fromJson_handles_complex_object() throws JsonProcessingException {
        String json = "{\"name\":\"test\",\"count\":42,\"active\":true}";
        TestObject result = JsonUtils.fromJson(json, TestObject.class);

        assertEquals("test", result.name());
        assertEquals(42, result.count());
        assertTrue(result.active());
    }

    @Test
    void toJson_handles_list() throws JsonProcessingException {
        List<String> list = List.of("one", "two", "three");
        String json = JsonUtils.toJson(list);

        assertTrue(json.contains("one"));
        assertTrue(json.contains("two"));
        assertTrue(json.contains("three"));
    }

    @Test
    void toJson_serializes_instant_as_iso_string() throws JsonProcessingException {
        Instant instant = Instant.parse("2024-01-15T10:30:00Z");
        String json = JsonUtils.toJson(Map.of("time", instant));

        assertTrue(json.contains("2024-01-15"));
    }

    @Test
    void getMapper_returns_singleton() {
        assertSame(JsonUtils.getMapper(), JsonUtils.getMapper());
    }

    @Test
    void fromJson_throws_on_invalid_json() {
        assertThrows(JsonProcessingException.class, () -> {
            JsonUtils.fromJson("not valid json", Map.class);
        });
    }

    // Test record for complex object testing
    record TestObject(String name, int count, boolean active) {}
}