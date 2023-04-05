package org.ielts.playground.model.entity.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

public class Map extends Text {
    private final java.util.Map<String, String> internal;

    public Map() {
        this.internal = new HashMap<>();
    }

    public Map(java.util.Map<String, String> internal) {
        this.internal = internal;
    }

    /**
     * Puts a value for a specific key.
     *
     * @param key the key.
     * @param value the value.
     */
    public void set(String key, String value) {
        this.internal.put(key, value);
    }

    /**
     * Retrieves a value for a specific key.
     * @param key the key.
     */
    public String get(String key) {
        return this.internal.get(key);
    }

    @SuppressWarnings("java:S2225")
    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this.internal);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }
}
