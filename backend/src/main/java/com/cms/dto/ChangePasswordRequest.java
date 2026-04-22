package com.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for PUT /auth/change-password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public
class ChangePasswordRequest {
    @NotBlank(message = "Current password is required")
    public String currentPassword;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 100, message = "New password must be 6–100 characters")
    public String newPassword;
}
