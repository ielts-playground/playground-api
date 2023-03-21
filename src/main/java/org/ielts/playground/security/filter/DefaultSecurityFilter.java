package org.ielts.playground.security.filter;

import org.ielts.playground.common.constant.PathConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Optional;

/**
 * The default security filter. It must be the last filter in the chain.
 */
public class DefaultSecurityFilter implements Filter, AuthenticationChangeable, PathAuthorizable {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final Authentication authentication = Optional
                .ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .orElse(new GuestAuthenticationToken());
        this.setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    @Override
    public String getPath() {
        return PathConstants.ALL_PATTERN;
    }
}
