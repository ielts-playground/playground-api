package org.ielts.playground.model.entity.converter;

import org.ielts.playground.common.enumeration.Subscription;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Optional;

@Converter(autoApply = true)
public class SubscriptionConverter implements AttributeConverter<Subscription, String> {
    @Override
    public String convertToDatabaseColumn(Subscription subscription) {
        return Optional.ofNullable(subscription)
                .map(Subscription::getValue)
                .orElse(null);
    }

    @Override
    public Subscription convertToEntityAttribute(String value) {
        return Subscription.of(value);
    }
}
