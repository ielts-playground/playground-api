package org.ielts.playground.model.entity.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

import java.util.Map;

public class Range extends Text {
    @Getter
    private final Long from;
    @Getter
    private final Long to;

    public Range(Long from, Long to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(Map.of(
                    "from", this.from,
                    "to", this.to
            ));
        } catch (JsonProcessingException ex) {
            return null;
        }
    }
}
