package com.cms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Phone sub-object (label + value)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class PhoneEntryDTO {
    @NotBlank(message = "Phone label is required")
    public String label;  // work, home, personal, other

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone")
    public String phone;
}
