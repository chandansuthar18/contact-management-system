package com.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body for login and register
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class AuthResponse {
    public String token;
    public String tokenType = "Bearer";
    public UserDTO user;
}
