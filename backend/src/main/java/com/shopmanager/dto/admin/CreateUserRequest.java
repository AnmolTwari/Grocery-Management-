package com.shopmanager.dto.admin;

import com.shopmanager.entity.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 50, message = "Username must be 3-50 characters")
        String username,
        @Email(message = "Enter a valid email address")
        @Size(max = 255, message = "Email must be at most 255 characters")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be 6-100 characters")
        String password,
        @NotNull(message = "Role is required") UserRole role) {
}