package org.ielts.playground.utils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.lang.Nullable;

/**
 * Contains some methods that support working with collections.
 */
public final class CollectionUtils {
    private CollectionUtils() {}

    /**
     * Retrieves an empty list if a list is {@code null}.
     *
     * @param <T> the type.
     * @param list the list.
     */
    public static final <T> List<T> emptyIfNull(@Nullable List<T> list) {
        return Optional.ofNullable(list).orElse(Collections.emptyList());
    }
}
