package com.cms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for PUT /users/me
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public
class UpdateUserRequest {
    @Size(max = 100)
    public String firstName;
    @Size(max = 100)
    public String lastName;
    @Email
    public String email;
    @Pattern(regexp = "^\\+?[0-9]{7,15}$")
    public String phone;
}
