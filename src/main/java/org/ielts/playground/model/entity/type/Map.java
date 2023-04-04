package org.ielts.playground.model.entity.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

public class Map extends Text {
    private final java.util.Map<String, String> map;

    public Map() {
        this.map = new HashMap<>();
    }

    /**
     * Puts a value for a specific key.
     *
     * @param key the key.
     * @param value the value.
     */
    public void set(String key, String value) {
        this.map.put(key, value);
    }

    /**
     * Retrieves a value for a specific key.
     * @param key the key.
     */
    public String get(String key) {
        return this.map.get(key);
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this.map);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }
}
