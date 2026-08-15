package com.shopmanager.dto.category;

import com.shopmanager.entity.Category;


public record CategoryResponse(Long id, String name) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}