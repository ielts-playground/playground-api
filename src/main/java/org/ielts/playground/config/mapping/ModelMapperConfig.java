package org.ielts.playground.config.mapping;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.validation.constraints.NotNull;

@Configuration
public class ModelMapperConfig {
    @Primary
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapperWrapper(new ModelMapper())
                .getMapper();
    }

    private static class ModelMapperWrapper {
        private final ModelMapper mapper;

        public ModelMapperWrapper(@NotNull ModelMapper mapper) {
            this.mapper = mapper;
        }

        public ModelMapper getMapper() {
            return this.mapper;
        }
    }
}
