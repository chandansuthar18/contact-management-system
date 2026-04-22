package com.cms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class LoginRequest {
    @NotBlank(message = "Email or phone is required")
    public String identifier;   // can be email or phone

    @NotBlank(message = "Password is required")
    public String password;
}
