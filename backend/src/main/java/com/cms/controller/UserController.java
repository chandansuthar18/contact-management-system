package com.cms.controller;

import com.cms.dto.*;
import com.cms.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * UserController — REST API for user profile management.
 *
 * Base path: /api/v1/users
 * All endpoints require a valid Bearer JWT token.
 *
 * Endpoints:
 *   GET  /users/me   → get logged-in user's profile
 *   PUT  /users/me   → update logged-in user's profile
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired private UserService userService;

    /**
     * GET /users/me
     *
     * Returns the current logged-in user's profile.
     * The user is identified from the JWT token (no ID needed in URL).
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.debug("Profile request for: {}", userDetails.getUsername());
        Long userId = userService.getUserIdByIdentifier(userDetails.getUsername());
        return ResponseEntity.ok(userService.getCurrentUser(userId));
    }

    /**
     * PUT /users/me
     *
     * Updates the current user's profile fields.
     * Only non-null fields in the request body are updated.
     *
     * Request body: { firstName?, lastName?, email?, phone? }
     */
    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateProfile(
            @Valid @RequestBody UpdateUserRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Profile update request for: {}", userDetails.getUsername());
        Long userId = userService.getUserIdByIdentifier(userDetails.getUsername());
        return ResponseEntity.ok(userService.updateProfile(userId, request));
    }
}
