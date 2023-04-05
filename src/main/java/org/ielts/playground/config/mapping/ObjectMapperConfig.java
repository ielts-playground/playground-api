package org.ielts.playground.config.mapping;

import java.io.IOException;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.ielts.playground.model.entity.type.Map;
import org.ielts.playground.model.entity.type.Range;
import org.ielts.playground.model.entity.type.Size;
import org.ielts.playground.model.entity.type.Text;
import org.ielts.playground.model.entity.type.TextDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

@Configuration
public class ObjectMapperConfig {
    @Primary
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapperWrapper(new ObjectMapper())
                .supportDeserializingText()
                .permitEmptyStringAsNullValue()
                .getMapper();
    }

    private static class ObjectMapperWrapper {
        private final ObjectMapper mapper;

        public ObjectMapperWrapper(@NotNull ObjectMapper mapper) {
            this.mapper = mapper;
        }

        public ObjectMapper getMapper() {
            return this.mapper;
        }

        @SuppressWarnings({ "java:S1186", "unchecked" })
        private static class TextJsonDeserializer<T extends Text> extends JsonDeserializer<T> {
            @Override
            public T deserialize(JsonParser parser, DeserializationContext context)
                    throws IOException {
                final Object value = parser.readValueAs(Object.class);
                if (!Objects.isNull(value)) {
                    return (T) TextDeserializer.parse(value);
                }
                return null;
            }
        }

        public ObjectMapperWrapper supportDeserializingText() {
            final SimpleModule module = new SimpleModule();
            module.addDeserializer(Range.class, new TextJsonDeserializer<>());
            module.addDeserializer(Size.class, new TextJsonDeserializer<>());
            module.addDeserializer(Map.class, new TextJsonDeserializer<>());
            module.addDeserializer(Text.class, new TextJsonDeserializer<>());
            this.mapper.registerModule(module);
            return this;
        }

        public ObjectMapperWrapper permitEmptyStringAsNullValue() {
            this.mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
            return this;
        }
    }
}
