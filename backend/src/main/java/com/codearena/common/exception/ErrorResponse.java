package com.codearena.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        boolean success,
        String message,
        int status,
        String path,
        Instant timestamp,
        Map<String, String> fieldErrors
) {
    public static ErrorResponse of(String message, int status, String path) {
        return new ErrorResponse(false, message, status, path, Instant.now(), null);
    }

    public static ErrorResponse validation(String message, int status, String path, Map<String, String> fieldErrors) {
        return new ErrorResponse(false, message, status, path, Instant.now(), fieldErrors);
    }
}
