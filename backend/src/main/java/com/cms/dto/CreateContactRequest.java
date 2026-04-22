package com.cms.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request body for POST /contacts (create)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class CreateContactRequest {
    @NotBlank(message = "First name is required")
    @Size(max = 100)
    public String firstName;

    @Size(max = 100)
    public String lastName;

    @Size(max = 100)
    public String title;

    @Valid
    public List<EmailEntryDTO> emails;

    @Valid
    public List<PhoneEntryDTO> phones;
}
