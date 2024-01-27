package org.ielts.playground.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ielts.playground.common.constant.CachingConstants;
import org.ielts.playground.model.dto.BasicUserDetails;
import org.ielts.playground.model.entity.User;
import org.ielts.playground.model.response.UserInfoResponse;
import org.ielts.playground.service.UserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    private final SecurityUtils self;
    private final UserService userService;

    public SecurityUtils(@Lazy SecurityUtils self, UserService userService) {
        this.self = self;
        this.userService = userService;
    }

    public String getLoggedUsername() {
        return Optional.ofNullable(getLoggedUserDetails())
                .map(UserDetails::getUsername)
                .orElse(null);
    }

    @Cacheable(key = "#username", unless =  "#result == null", cacheNames = CachingConstants.USER_INFO_CACHE_NAME)
    public Long getUserIdFromUsername(String username) {
        return this.userService.getUserInfo(username)
                .map(UserInfoResponse::getId)
                .orElse(null);
    }

    public Long getLoggedUserId() {
        return Optional.ofNullable(getLoggedUserDetails())
                .map(BasicUserDetails.class::cast)
                .map(BasicUserDetails::getId)
                .orElse(null);
    }

    public Collection<String> getLoggedAuthorities() {
        return Optional.ofNullable(getLoggedUserDetails())
                .map(UserDetails::getAuthorities)
                .orElse(Collections.emptyList())
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    private UserDetails getLoggedUserDetails() {
        return Optional.ofNullable(SecurityContextHolder.getContext()
                .getAuthentication())
                .map(Authentication::getPrincipal)
                .map(principal -> {
                    if (principal instanceof UserDetails) {
                        return (UserDetails) principal;
                    }
                    if (principal instanceof String) {
                        final String username = principal.toString();
                        return new BasicUserDetails(User.builder()
                                .username(username)
                                .id(this.self.getUserIdFromUsername(username))
                                .build());
                    }
                    return null;
                })
                .orElse(null);
    }
}
