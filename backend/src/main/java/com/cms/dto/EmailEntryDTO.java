package com.cms.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Email sub-object (label + value)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class EmailEntryDTO {
    @NotBlank(message = "Email label is required")
    public String label;  // work, personal, other

    @NotBlank
    @Email(message = "Invalid email")
    public String email;
}
