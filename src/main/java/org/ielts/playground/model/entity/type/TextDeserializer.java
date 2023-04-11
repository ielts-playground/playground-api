package org.ielts.playground.model.entity.type;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.NotNull;

public final class TextDeserializer {
    private TextDeserializer() {
    }

    /**
     * Tries converting a text to a {@link Text}.
     *
     * @param value the text.
     * @return the {@link Text} if converted; otherwise. {@code null}.
     */
    @SuppressWarnings("unchecked")
    public static Text parse(@NotNull Object value) {
        final List<Class<? extends Text>> classes = Arrays.asList(
                Range.class,
                Size.class);
        final ObjectMapper mapper = new ObjectMapper();
        for (Class<? extends Text> clazz : classes) {
            try {
                return mapper.convertValue(value, clazz);
            } catch (IllegalArgumentException ex) {
                // ignore
            }
        }
        try {
            return new Map(mapper.convertValue(value, java.util.Map.class));
        } catch (IllegalArgumentException ex) {
            // ignore
        }
        return new Raw(value.toString());
    }
}
