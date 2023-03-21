package org.ielts.playground.security.filter;

public interface PathAuthorizable {
    /**
     * Retrieves a URL pattern that needs to be authenticated.
     */
    String getPath();
}
