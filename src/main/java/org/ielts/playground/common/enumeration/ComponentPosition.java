package org.ielts.playground.common.enumeration;

import java.util.Arrays;

public enum ComponentPosition {
    RIGHT("1"),
    LEFT("0");

    private final String value;

    ComponentPosition(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public static ComponentType of(String value) {
        return Arrays.stream(ComponentType.values())
                .filter(componentType -> componentType.getValue().equals(value))
                .findAny()
                .orElse(null);
    }
}
