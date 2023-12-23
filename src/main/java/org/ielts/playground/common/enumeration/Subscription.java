package org.ielts.playground.common.enumeration;

import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.Objects;

@Getter
public enum Subscription {
    FREE("FREE"),
    PREMIUM("PREMIUM");

    private final String value;

    Subscription(String value) {
        this.value = value;
    }

    @Nullable
    public static Subscription of(@Nullable String value) {
        for (Subscription subscription : Subscription.values()) {
            if (Objects.equals(subscription.value, value)) {
                return subscription;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.value;
    }
}
