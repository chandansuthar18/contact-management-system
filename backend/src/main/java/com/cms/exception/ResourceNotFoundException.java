package com.cms.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// ─────────────────────────────────────────────────────────────────────
// Custom Application Exceptions
// Each exception maps to an HTTP status code via @ResponseStatus.
// The GlobalExceptionHandler catches these and formats the response.
// ─────────────────────────────────────────────────────────────────────

/** Thrown when a requested resource (user/contact) is not found → 404 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public
class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
    public ResourceNotFoundException(String resourceName, Long id) {
        super(resourceName + " not found with id: " + id);
    }
}

/** General service-layer exception → 500 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class ServiceException extends RuntimeException {
    public ServiceException(String message) {
        super(message);
    }
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
