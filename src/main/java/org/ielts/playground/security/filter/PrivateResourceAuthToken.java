package org.ielts.playground.security.filter;

import java.util.Collection;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

public class PrivateResourceAuthToken extends AbstractAuthenticationToken {
    private final String client;

    public PrivateResourceAuthToken(String client, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.client = client;
        this.setAuthenticated(true);
    }

    @Override
    public Object getPrincipal() {
        return this.client;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
