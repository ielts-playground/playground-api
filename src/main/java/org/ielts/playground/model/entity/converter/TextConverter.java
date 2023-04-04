package org.ielts.playground.model.entity.converter;

import org.ielts.playground.model.entity.type.Text;
import org.ielts.playground.model.entity.type.TextDeserializer;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Optional;

@Component
@Converter
public class TextConverter implements AttributeConverter<Text, String> {
    @Override
    public String convertToDatabaseColumn(Text text) {
        return Optional.ofNullable(text)
                .map(Text::toString)
                .orElse(null);
    }

    @Override
    public Text convertToEntityAttribute(String value) {
        return TextDeserializer.parse(value);
    }
}
