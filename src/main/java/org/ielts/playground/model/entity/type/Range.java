package org.ielts.playground.model.entity.type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Range extends Text {
    public static final Range GENESIS;

    static {
        GENESIS = new Range(0L, 0L, null);
    }

    @NotNull
    private Long from;

    @NotNull
    private Long to;

    @Nullable
    private Range and;

    @SuppressWarnings("java:S2225")
    @Override
    public String toString() {
        try {
            final Map<String, Object> map = new HashMap<>();
            map.put("from", this.from);
            map.put("to", this.to);
            if (Objects.nonNull(this.and)) {
                map.put("and", this.and);
            }
            return new ObjectMapper().writeValueAsString(map);
        } catch (JsonProcessingException ex) {
            return null;
        }
    }
}
