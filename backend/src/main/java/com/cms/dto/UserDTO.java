package com.cms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Safe view of a user (no password)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public
class UserDTO {
    public Long id;
    public String firstName;
    public String lastName;
    public String email;
    public String phone;
    public LocalDateTime createdAt;
}
