package com.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard success message response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public
class MessageResponse {
    public String message;
}
