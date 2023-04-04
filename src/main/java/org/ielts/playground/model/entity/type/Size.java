package org.ielts.playground.model.entity.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.Map;

public class Size extends Text {
    @Getter
    private final Double width;
    @Getter
    private final Double height;

    public Size(Double width, Double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(Map.of(
                    "width", this.width,
                    "height", this.height
            ));
        } catch (JsonProcessingException ex) {
            return null;
        }
    }
}
