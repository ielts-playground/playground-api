package org.ielts.playground.model.entity.type;

import org.springframework.lang.Nullable;

public interface Numerable {
    /**
     * @return the {@link String} value.
     */
    String raw();

    /**
     * @return its {@link Long} value.
     */
    @Nullable
    default Long longValue() {
        try {
            return Long.parseLong(this.raw());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}