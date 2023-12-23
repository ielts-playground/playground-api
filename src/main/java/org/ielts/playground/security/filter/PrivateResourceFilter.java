package org.ielts.playground.security.filter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ielts.playground.utils.PrivateResourceUtils;

public class PrivateResourceFilter implements Filter, AuthenticationChangeable, PathAuthorizable {

    private final ServletRequestChecker requestChecker;
    private final PrivateResourceUtils utils;

    public PrivateResourceFilter(
            ServletRequestChecker requestChecker,
            PrivateResourceUtils utils) {
        this.requestChecker = requestChecker;
        this.utils = utils;
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Optional.ofNullable(this.requestChecker.retrievePublicPermission(request))
                .ifPresentOrElse(this::setAuthentication, () -> {
                    final String client = utils.retrieveRequestClient(request);
                    final String[] clients = this.requestChecker.retrievePermittedClients(request);
                    final boolean permitted = Optional.ofNullable(clients)
                            .map(group -> new HashSet<>(Arrays.asList(clients)).contains(client))
                            .orElse(Objects.nonNull(client));
                    if (permitted) {
                        this.setAuthentication(
                            new PrivateResourceAuthToken(client, Collections.emptyList())
                        );
                    } else {
                        this.setAuthentication(null);
                    }
                });
        chain.doFilter(request, response);
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
