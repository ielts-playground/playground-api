package org.ielts.playground.model.entity.converter;

import org.ielts.playground.common.constant.DateTimeConstants;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Component
@Converter
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern(DateTimeConstants.DATE_TIME_PATTERN);

    @Override
    public String convertToDatabaseColumn(LocalDateTime dateTime) {
        return FORMATTER.print(dateTime);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String text) {
        return LocalDateTime.parse(text, FORMATTER);
    }
}
