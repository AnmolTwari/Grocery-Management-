package com.grocery.manager.dto.category;

import com.grocery.manager.entity.Category;

/** API representation of a category. */
public record CategoryResponse(Long id, String name) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}