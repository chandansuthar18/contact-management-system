package com.cms.controller;

import com.cms.dto.*;
import com.cms.service.AuthService;
import com.cms.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — handles authentication endpoints.
 *
 * Base path: /api/v1/auth
 *
 * Endpoints:
 *   POST  /auth/register          → register new user
 *   POST  /auth/login             → login
 *   PUT   /auth/change-password   → change password (protected)
 *
 * @Valid triggers bean validation on request body.
 * @AuthenticationPrincipal injects the logged-in user's UserDetails.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired private AuthService authService;
    @Autowired private UserService userService;

    /**
     * POST /auth/register
     * Register a new user account.
     *
     * Request body: { firstName, lastName, email, phone, password }
     * Response:     { token, tokenType, user }
     * HTTP Status:  201 Created
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Register request received for email={}", request.getEmail());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /auth/login
     * Authenticate an existing user.
     *
     * Request body: { identifier (email or phone), password }
     * Response:     { token, tokenType, user }
     * HTTP Status:  200 OK
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for identifier={}", request.getIdentifier());
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /auth/change-password
     * Change the logged-in user's password.
     * Requires Bearer token in Authorization header.
     *
     * Request body: { currentPassword, newPassword }
     * Response:     { message }
     * HTTP Status:  200 OK
     */
    @PutMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long userId = userService.getUserIdByIdentifier(userDetails.getUsername());
        authService.changePassword(userId, request);
        return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
    }
}
