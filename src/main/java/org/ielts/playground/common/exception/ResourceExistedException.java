package org.ielts.playground.common.exception;

import org.springframework.http.HttpStatus;

/**
 * An exception thrown when a resource has already existed.
 */
public class ResourceExistedException extends BaseWebException {
    public ResourceExistedException() {
        super(HttpStatus.BAD_REQUEST);
    }

    public ResourceExistedException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }

    public ResourceExistedException(String message, Throwable cause) {
        super(HttpStatus.BAD_REQUEST, message, cause);
    }
}
