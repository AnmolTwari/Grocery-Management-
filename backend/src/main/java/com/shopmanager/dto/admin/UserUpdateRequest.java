package com.shopmanager.dto.admin;

import com.shopmanager.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        UserRole role,
        @Email(message = "Enter a valid email address")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email,
        @NotNull(message = "Enabled flag is required") Boolean enabled) {
}
