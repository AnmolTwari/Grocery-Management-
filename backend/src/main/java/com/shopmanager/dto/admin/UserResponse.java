package com.shopmanager.dto.admin;

import java.time.LocalDateTime;

import com.shopmanager.entity.User;
import com.shopmanager.entity.UserRole;

public record UserResponse(
        Long id,
        String username,
        String email,
        UserRole role,
        boolean enabled,
        LocalDateTime createdAt,
        String passwordHash) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole() != null ? user.getRole() : UserRole.USER,
                user.isEnabled(),
                user.getCreatedAt(),
                user.getPassword());
    }
}
