package org.ielts.playground.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.RequestParameterBuilder;
import springfox.documentation.service.ParameterType;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Arrays;

@EnableSwagger2
@Configuration
@ConfigurationProperties(prefix = "swagger")
@Profile({ "dev", "default" })
public class SwaggerConfig {
    /**
     * The Swagger's base URL.
     */
    private String baseUrl;

    private static final String AUTHORIZATION_HEADER_KEY = "Authorization";

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .host(this.baseUrl)
                .globalRequestParameters(Arrays.asList(
                        new RequestParameterBuilder()
                                .name(AUTHORIZATION_HEADER_KEY)
                                .in(ParameterType.HEADER)
                                .build(),
                        new RequestParameterBuilder()
                                .name("X-Api-Key") // for private resources
                                .in(ParameterType.HEADER)
                                .build()
                ))
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.any())
                .build();
    }
}
