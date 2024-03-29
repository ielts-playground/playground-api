package org.ielts.playground.model.entity.converter;

import org.ielts.playground.common.constant.DateTimeConstants;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Optional;

@Converter(autoApply = true)
public class LocalDateTimeConverter implements AttributeConverter<LocalDateTime, String> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern(DateTimeConstants.DATE_TIME_PATTERN);

    @Override
    public String convertToDatabaseColumn(LocalDateTime dateTime) {
        return Optional.ofNullable(dateTime)
                .map(FORMATTER::print)
                .orElse(null);
    }

    @Override
    public LocalDateTime convertToEntityAttribute(String text) {
        return Optional.ofNullable(text)
                .map(t -> LocalDateTime.parse(t, FORMATTER))
                .orElse(null);
    }
}
