package com.cms.exception;

import com.cms.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * GlobalExceptionHandler
 *
 * Catches ALL exceptions thrown anywhere in the application and returns
 * a clean JSON error response instead of an ugly stack trace.
 *
 * Annotations:
 *   @RestControllerAdvice  = applies to all @RestController classes
 *   @ExceptionHandler      = maps specific exception type to handler method
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /** 404 — Resource not found */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());
        return new ErrorResponse("NOT_FOUND", ex.getMessage());
    }

    /** 409 — Duplicate record (email/phone already exists) */
    @ExceptionHandler(DuplicateResourceException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicate(DuplicateResourceException ex) {
        log.warn("Duplicate resource: {}", ex.getMessage());
        return new ErrorResponse("CONFLICT", ex.getMessage());
    }

    /** 401 — Bad login credentials */
    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorized(RuntimeException ex) {
        log.warn("Unauthorized access attempt: {}", ex.getMessage());
        return new ErrorResponse("UNAUTHORIZED", "Invalid credentials");
    }

    /** 403 — Trying to access another user's data */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleForbidden(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return new ErrorResponse("FORBIDDEN", "Access denied");
    }

    /**
     * 400 — Validation errors from @Valid annotations.
     * Collects all field errors into one message.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.joining(", "));
        log.warn("Validation failed: {}", errors);
        return new ErrorResponse("VALIDATION_ERROR", errors);
    }

    /** 400 — General bad request */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return new ErrorResponse("BAD_REQUEST", ex.getMessage());
    }

    /** 500 — Catch-all for unexpected errors */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleGeneral(Exception ex) {
        log.error("Unexpected server error: {}", ex.getMessage(), ex);
        return new ErrorResponse("INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please try again later.");
    }
}
