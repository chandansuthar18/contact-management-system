package com.cms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when user tries to access another user's data → 403
 */
@ResponseStatus(HttpStatus.FORBIDDEN)
public
class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
