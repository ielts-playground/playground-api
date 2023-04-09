package org.ielts.playground.repository;

import org.ielts.playground.model.entity.Component;
import org.ielts.playground.model.entity.converter.ComponentTypeConverter;
import org.ielts.playground.model.entity.converter.TextConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Repository
public class ComponentWriteRepository extends AbstractWriteRepository<Component> {
    private final ComponentTypeConverter componentTypeConverter;
    private final TextConverter textConverter;

    protected ComponentWriteRepository(
            ComponentTypeConverter componentTypeConverter,
            TextConverter textConverter,
            JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
        this.componentTypeConverter = componentTypeConverter;
        this.textConverter = textConverter;
    }

    @Override
    public void saveAll(Collection<Component> components) {
        final List<String> columns = Arrays.asList(
                "part_id",
                "position",
                "type",
                "kei",
                "value",
                "size",
                "options"
        );
        final String placeholder = String.format(
                "(%s)",
                duplicateAndJoin(columns.size(), "?", ", "));
        String sql = String.format(
                "INSERT INTO component (%s) VALUES %s",
                String.join(", ", columns),
                duplicateAndJoin(components.size(), placeholder, ", "));

        jdbcTemplate.update(sql, ps -> {
            int pos = 1; // param index starts with 1
            for (Component component : components) {
                ps.setLong(pos++, component.getPartId());
                ps.setString(pos++, component.getPosition());
                ps.setString(pos++, this.componentTypeConverter.convertToDatabaseColumn(component.getType()));
                ps.setString(pos++, component.getKei());
                ps.setString(pos++, this.textConverter.convertToDatabaseColumn(component.getValue()));
                ps.setString(pos++, this.textConverter.convertToDatabaseColumn(component.getSize()));
                ps.setString(pos++, this.textConverter.convertToDatabaseColumn(component.getOptions()));
            }
        });
    }
}
