package com.cms.service;

import com.cms.config.JwtUtil;
import com.cms.dto.*;
import com.cms.entity.User;
import com.cms.exception.*;
import com.cms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * AuthService — handles all authentication business logic.
 *
 * Responsibilities:
 *   - Validate registration input (email/phone uniqueness)
 *   - Hash passwords with BCrypt
 *   - Issue JWT tokens after successful auth
 *   - Validate current password before changing it
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtUtil jwtUtil;

    /**
     * Register a new user.
     *
     * Steps:
     *   1. Validate that email OR phone is provided
     *   2. Check for duplicates
     *   3. Hash the password
     *   4. Save user to DB
     *   5. Generate and return JWT
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: email={}, phone={}", request.getEmail(), request.getPhone());

        // At least one of email or phone must be provided
        if (!StringUtils.hasText(request.getEmail()) && !StringUtils.hasText(request.getPhone())) {
            throw new IllegalArgumentException("Either email or phone number is required");
        }

        // Check for duplicate email
        if (StringUtils.hasText(request.getEmail()) &&
            userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        // Check for duplicate phone
        if (StringUtils.hasText(request.getPhone()) &&
            userRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone already registered: " + request.getPhone());
        }

        // Build and save the user
        User user = User.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phone(request.getPhone())
            .password(passwordEncoder.encode(request.getPassword()))  // BCrypt hash
            .build();

        User saved = userRepository.save(user);
        log.info("User registered successfully: id={}", saved.getId());

        // Generate JWT using email or phone as the subject
        String principal = saved.getEmail() != null ? saved.getEmail() : saved.getPhone();
        String token = jwtUtil.generateToken(principal);

        return AuthResponse.builder()
            .token(token)
            .user(toUserDTO(saved))
            .build();
    }

    /**
     * Authenticate an existing user (login).
     *
     * Steps:
     *   1. Find user by email or phone
     *   2. Verify the password against the BCrypt hash
     *   3. Return a fresh JWT
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for identifier: {}", request.getIdentifier());

        // Find by email or phone
        User user = userRepository.findByEmail(request.getIdentifier())
            .or(() -> userRepository.findByPhone(request.getIdentifier()))
            .orElseThrow(() -> {
                log.warn("Login failed - user not found: {}", request.getIdentifier());
                return new InvalidCredentialsException("Invalid email/phone or password");
            });

        // BCrypt check: matches(plaintext, hash)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            log.warn("Login failed - wrong password for: {}", request.getIdentifier());
            throw new InvalidCredentialsException("Invalid email/phone or password");
        }

        String principal = user.getEmail() != null ? user.getEmail() : user.getPhone();
        String token = jwtUtil.generateToken(principal);

        log.info("User logged in successfully: id={}", user.getId());
        return AuthResponse.builder().token(token).user(toUserDTO(user)).build();
    }

    /**
     * Change a user's password.
     *
     * Steps:
     *   1. Verify current password
     *   2. Hash the new password
     *   3. Save
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        log.info("Password change request for userId={}", userId);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for userId={}", userId);
    }

    /** Convert User entity → UserDTO (safe, no password) */
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
