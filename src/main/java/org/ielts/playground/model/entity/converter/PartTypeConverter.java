package org.ielts.playground.model.entity.converter;

import org.springframework.stereotype.Component;
import org.ielts.playground.common.enumeration.PartType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Optional;

@Component
@Converter
public class PartTypeConverter implements AttributeConverter<PartType, String> {
    @Override
    public String convertToDatabaseColumn(PartType partType) {
        return Optional.ofNullable(partType)
                .map(PartType::getValue)
                .orElse(null);
    }

    @Override
    public PartType convertToEntityAttribute(String value) {
        return PartType.of(value);
    }
}
