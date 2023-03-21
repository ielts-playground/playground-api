package org.ielts.playground.common.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends BaseWebException {
    public InternalServerException() {
        super(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    public InternalServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }
}
