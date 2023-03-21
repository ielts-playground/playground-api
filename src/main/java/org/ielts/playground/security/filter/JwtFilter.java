package org.ielts.playground.security.filter;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.ielts.playground.common.constant.AuthorityConstants;
import org.ielts.playground.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;

public class JwtFilter implements Filter, AuthenticationChangeable, PathAuthenticable {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final ServletRequestChecker requestChecker;
    private final UserDetailsService userDetailsService;
    private final JwtUtils utils;

    public JwtFilter(
            ServletRequestChecker requestChecker,
            JwtUtils utils, UserDetailsService userDetailsService) {
        this.requestChecker = requestChecker;
        this.utils = utils;
        this.userDetailsService = userDetailsService;
    }

    private String getTokenFromRequest(HttpServletRequest request) {
        Optional<String> authorization = Optional.ofNullable(
                request.getHeader(AUTHORIZATION_HEADER));

        if (authorization.isPresent()) {
            String token = authorization.get();
            if (token.startsWith(BEARER_PREFIX)) {
                return token.substring(BEARER_PREFIX.length());
            }
        }

        return null;
    }

    private Authentication validate(String username, String token) {
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
        if (utils.isTokenValid(token, userDetails)) {
            return new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities());
        }
        return null;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Optional.ofNullable(this.requestChecker.retrievePublicPermission(request))
                .ifPresentOrElse(this::setAuthentication, () -> {
                    Optional<String> token = Optional.ofNullable(getTokenFromRequest(request));
                    if (token.isPresent()) {
                        try {
                            String username = this.utils.getUsernameFromToken(token.get());
                            Optional.ofNullable(this.validate(username, token.get())).ifPresent(authentication -> {
                                this.setAuthentication(authentication);
                                if (this.requestChecker.isAdminPermitted(request)) {
                                    boolean authorized = authentication.getAuthorities().stream()
                                            .map(GrantedAuthority::getAuthority)
                                            .anyMatch(authority -> Objects.equals(authority, AuthorityConstants.ROLE_ADMIN));
                                    if (!authorized) {
                                        this.setAuthentication(null);
                                    }
                                }
                            });
                        } catch (ExpiredJwtException ex) {
                            this.setAuthentication(null);
                        }
                    } else {
                        this.setAuthentication(null);
                    }
                });
        filterChain.doFilter(request, response);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        this.doFilterInternal((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    @Override
    public String getPath() {
        return this.utils.getPath();
    }
}
