package com.cms.service;

import com.cms.dto.*;
import com.cms.entity.User;
import com.cms.exception.*;
import com.cms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * UserService — manages user profile operations.
 *
 * Responsibilities:
 *   - Fetch user profile by ID
 *   - Update profile fields (name, email, phone)
 *   - Validate uniqueness of new email/phone on update
 */
@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired private UserRepository userRepository;

    /**
     * Get the current user's profile.
     * Called by GET /users/me
     */
    public UserDTO getCurrentUser(Long userId) {
        log.debug("Fetching profile for userId={}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return toUserDTO(user);
    }

    /**
     * Update the current user's profile.
     * Only updates fields that are non-null in the request.
     */
    @Transactional
    public UserDTO updateProfile(Long userId, UpdateUserRequest request) {
        log.info("Updating profile for userId={}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Update first/last name if provided
        if (StringUtils.hasText(request.getFirstName())) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null)                user.setLastName(request.getLastName());

        // Update email: check it's not taken by another user
        if (StringUtils.hasText(request.getEmail())
                && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Email already in use: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        // Update phone: check it's not taken by another user
        if (StringUtils.hasText(request.getPhone())
                && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new DuplicateResourceException("Phone already in use: " + request.getPhone());
            }
            user.setPhone(request.getPhone());
        }

        User updated = userRepository.save(user);
        log.info("Profile updated for userId={}", userId);
        return toUserDTO(updated);
    }

    /**
     * Resolve the logged-in user's ID from their principal name (email or phone).
     * Used by controllers to get the numeric ID from the SecurityContext username.
     */
    public Long getUserIdByIdentifier(String identifier) {
        return userRepository.findByEmail(identifier)
            .or(() -> userRepository.findByPhone(identifier))
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + identifier))
            .getId();
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
            .id(user.getId())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .createdAt(user.getCreatedAt())
            .build();
    }
}
