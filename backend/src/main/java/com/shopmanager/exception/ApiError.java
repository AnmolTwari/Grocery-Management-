package com.shopmanager.exception;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;


public record ApiError(String timestamp, int status, String error, String message, String path,
        Map<String, String> fieldErrors) {

    public static ApiError of(int status, String error, String message, String path,
            Map<String, String> fieldErrors) {
        return new ApiError(OffsetDateTime.now(ZoneOffset.UTC).toString(), status, error, message,
                path, fieldErrors);
    }
}