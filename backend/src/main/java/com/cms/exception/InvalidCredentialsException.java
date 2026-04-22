package com.cms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown for invalid credentials during login → 401
 */
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public
class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
