package org.ielts.playground.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import org.ielts.playground.model.dto.BasicUserDetails;
import org.ielts.playground.model.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    public String getLoggedUsername() {
        return Optional.ofNullable(getLoggedUserDetails())
                .map(UserDetails::getUsername)
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
                        return new BasicUserDetails(User.builder()
                                .username(principal.toString())
                                .build());
                    }
                    return null;
                })
                .orElse(null);
    }
}
