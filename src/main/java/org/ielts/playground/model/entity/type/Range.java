package org.ielts.playground.model.entity.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Range extends Text {
    private Long from;
    private Long to;
    private Range and;

    /**
     * The first range in a test's part (optional).
     */
    public static final Range NONE;

    static {
        NONE = new Range(0L, 0L, null);
    }

    @SuppressWarnings("java:S2225")
    @Override
    public String toString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }
}
