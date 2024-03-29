package org.ielts.playground.model.entity.converter;

import org.ielts.playground.common.enumeration.ComponentType;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Optional;

@Lazy
@Component
@Converter(autoApply = true)
public class ComponentTypeConverter implements AttributeConverter<ComponentType, String> {
    @Override
    public String convertToDatabaseColumn(ComponentType componentType) {
        return Optional.ofNullable(componentType)
                .map(ComponentType::getValue)
                .orElse(null);
    }

    @Override
    public ComponentType convertToEntityAttribute(String value) {
        return ComponentType.of(value);
    }
}
