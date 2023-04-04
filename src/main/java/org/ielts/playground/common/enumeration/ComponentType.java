package org.ielts.playground.common.enumeration;

import java.util.Arrays;

public enum ComponentType {
    TEXT("text"),
    TITLE("title"),
    IMAGE("image"),
    OPTIONS("options"),
    LIST("list"),
    QUESTION("question"),
    BOX("box"),
    FOOTNOTE("footnote"),
    RANGE("range"),
    BREAK("break");

    private final String value;

    ComponentType(String value) {
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
