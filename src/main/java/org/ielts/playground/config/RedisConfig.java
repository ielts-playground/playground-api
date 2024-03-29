package org.ielts.playground.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.Setter;

import org.ielts.playground.common.constant.CachingConstants;

@EnableCaching
@Configuration
public class RedisConfig {

    private final RedisProperties redisProperties;

    public RedisConfig(RedisProperties redisProperties) {
        this.redisProperties = redisProperties;
    }

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties(prefix = "spring.redis")
    public static class RedisProperties {
        private String host;
        private Integer port;
        private String password;
        private TimeToLive ttl;

        @Getter
        @Setter
        public static class TimeToLive {
            private Long defaultInSeconds;
            private Long postsView;
            private Long postsSearch;
            private Long testAudioGet;
            private Long userInfo;
        }
    }

    @Bean
    public <T> RedisTemplate<String, T> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, T> template = new RedisTemplate<>();
        ObjectMapper objectMapper = new ObjectMapper();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer(objectMapper));
        template.setEnableDefaultSerializer(false);
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        RedisCacheConfiguration defaultConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(redisProperties.getTtl().getDefaultInSeconds()));
        RedisCacheConfiguration postsViewConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(redisProperties.getTtl().getPostsView()));
        RedisCacheConfiguration postsSearchConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(redisProperties.getTtl().getPostsSearch()));
        RedisCacheConfiguration testAudioGetConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(redisProperties.getTtl().getTestAudioGet()));
        RedisCacheConfiguration userInfoConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(redisProperties.getTtl().getUserInfo()));

        return RedisCacheManager
                .builder(factory)
                .cacheDefaults(defaultConfiguration)
                .withCacheConfiguration(CachingConstants.POST_VIEW_CACHE_NAME, postsViewConfiguration)
                .withCacheConfiguration(CachingConstants.POST_SEARCH_CACHE_NAME, postsSearchConfiguration)
                .withCacheConfiguration(CachingConstants.TEST_AUDIO_GET_CACHE_NAME, testAudioGetConfiguration)
                .withCacheConfiguration(CachingConstants.USER_INFO_CACHE_NAME, userInfoConfiguration)
                .build();
    }
}
