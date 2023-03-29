package org.ielts.playground.utils;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Lazy
@Component
public class PrivateResourceUtils {

    private final PrivateProperties properties;
    private final Map<String, PrivateProperties.SecretKey> keyMap;

    public PrivateResourceUtils(PrivateProperties properties) {
        this.properties = properties;
        this.keyMap = new HashMap<>();
    }

    @PostConstruct
    public void initialize() {
        Optional.ofNullable(this.properties.secretKeys).ifPresent(keys -> {
            keys.forEach(key -> this.keyMap.put(key.getValue(), key));
        });
    }

    @Getter
    @Setter
    @Configuration
    @ConfigurationProperties(prefix = "server.security.authentication.private")
    public static class PrivateProperties implements Serializable {
        /**
         * The URL pattern that needs to apply the filter.
         */
        private String path;
        /**
         * The key in the Request Headers that contains the secret key.
         */
        private String headerKey;
        /**
         * A lists of valid secret keys.
         */
        private List<SecretKey> secretKeys;

        @Getter
        @Setter
        public static class SecretKey implements Serializable {
            /**
             * The secret key.
             */
            private String value;
            /**
             * Indicates the client using the secret key/
             */
            private String name;
        }
    }

    public String getHeaderKey() {
        return this.properties.getHeaderKey();
    }

    public String retrieveRequestClient(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(this.properties.getHeaderKey()))
                .flatMap(key -> Optional.ofNullable(this.keyMap.get(key))
                .map(PrivateProperties.SecretKey::getName))
                .orElse(null);
    }

    public String getPath() {
        return this.properties.getPath();
    }
}
