package org.ielts.playground.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;

public abstract class AbstractWriteRepository<E> {
    protected final JdbcTemplate jdbcTemplate;

    protected AbstractWriteRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Saves a collection of entities with multi-row insertion mechanism.
     *
     * @param entities the collection of entities.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public abstract void saveAll(Collection<E> entities);

    protected static String duplicateAndJoin(int total, @NotNull String str, @NotNull String delimiter) {
        List<String> ls = new ArrayList<>();
        for (int count = 0; count < total; count++) {
            ls.add(str);
        }
        return String.join(delimiter, ls);
    }
}
