package org.ielts.playground.service.impl;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import org.ielts.playground.common.constant.ContentGeneratingConstants;
import org.ielts.playground.service.ContentGeneratingService;
import lombok.Getter;
import lombok.Setter;

@Service(ContentGeneratingConstants.LOREM_IPSUM_GENERATOR)
public class LoremIpsumContentGeneratingService implements ContentGeneratingService {

    private final LoremIpsumContentGeneratorProperties properties;
    private final RestTemplate restTemplate;

    public LoremIpsumContentGeneratingService(LoremIpsumContentGeneratorProperties properties) {
        this.properties = properties;
        this.restTemplate = new RestTemplate();
    }

    @Configuration
    @ConfigurationProperties(prefix = "api.posts.generator.lorem-ipsum")
    @Setter
    @Getter
    public static class LoremIpsumContentGeneratorProperties {
        private String url;
    }

    @Override
    public String generate() {
        return this.restTemplate.getForObject(properties.getUrl(), String.class);
    }
}
