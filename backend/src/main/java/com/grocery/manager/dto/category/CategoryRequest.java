package com.grocery.manager.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for creating a category. */
public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        @Size(max = 100, message = "Category name must be at most 100 characters")
        String name) {

    public CategoryRequest {
        if (name != null) {
            name = name.trim();
        }
    }
}