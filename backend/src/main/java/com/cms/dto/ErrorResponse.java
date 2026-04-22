package com.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard API error response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public
class ErrorResponse {
    public String code;
    public String message;
    public LocalDateTime timestamp;

    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
