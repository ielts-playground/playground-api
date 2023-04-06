package org.ielts.playground.model.entity.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Range extends Text {
    private Long from;
    private Long to;

    @SuppressWarnings("java:S2225")
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