package com.example.todoapi.exception;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standard error body returned by the API when something goes wrong.
 */
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> validationErrors
) {
}
