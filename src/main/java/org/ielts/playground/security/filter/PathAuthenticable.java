package org.ielts.playground.security.filter;

public interface PathAuthenticable {
    /**
     * Retrieves a URL pattern that needs to be authenticated.
     */
    String getPath();
}
