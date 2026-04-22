package com.cms.dto;

// ─────────────────────────────────────────────────────────────
// DTOs (Data Transfer Objects)
// These classes carry data between Controller ↔ Service ↔ Client
// They are separate from entities to control what data is exposed.
// ─────────────────────────────────────────────────────────────

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

// ─── Auth DTOs ──────────────────────────────────────────────────

/** Request body for POST /auth/register */
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public
class RegisterRequest {

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name max 100 characters")
    public String firstName;

    @Size(max = 100)
    public String lastName;

    // Email OR phone is required — validated in service
    @Email(message = "Invalid email format")
    public String email;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number")
    public String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be 6–100 characters")
    public String password;
}

// ─── User DTOs ──────────────────────────────────────────────────

// ─── Contact DTOs ──────────────────────────────────────────────

