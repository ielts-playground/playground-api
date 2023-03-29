package org.ielts.playground.common.enumeration;

import java.util.Arrays;

public enum PartType {
    READING("reading"),
    LISTENING("listening"),
    WRITING("writing");

    private final String value;

    PartType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static PartType of(String value) {
        return Arrays.stream(PartType.values())
                .filter(componentType -> componentType.getValue().equals(value))
                .findAny()
                .orElse(null);
    }
}
