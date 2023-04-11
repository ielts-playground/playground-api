package org.ielts.playground.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class AsyncConfig implements AsyncConfigurer {
    private final Properties properties;

    public AsyncConfig(Properties properties) {
        this.properties = properties;
    }

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties(prefix = "server.async")
    public static class Properties {
        private Integer corePoolSize;
        private Integer maxPoolSize;
        private Integer queueCapacity;
    }

    @Override
    public Executor getAsyncExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        if (Objects.nonNull(this.properties)) {
            Optional.ofNullable(this.properties.getCorePoolSize())
                    .ifPresent(executor::setCorePoolSize);
            Optional.ofNullable(this.properties.getMaxPoolSize())
                    .ifPresent(executor::setMaxPoolSize);
            Optional.ofNullable(this.properties.getQueueCapacity())
                    .ifPresent(executor::setQueueCapacity);
        }
        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }
}
