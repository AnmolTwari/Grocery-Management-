package com.grocery.manager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grocery.manager.dto.category.CategoryRequest;
import com.grocery.manager.dto.category.CategoryResponse;
import com.grocery.manager.entity.Category;
import com.grocery.manager.exception.DuplicateResourceException;
import com.grocery.manager.repository.CategoryRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findAllByOrderByNameAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String name = request.name().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new DuplicateResourceException("CATEGORY_EXISTS",
                    "Category '" + name + "' already exists", "name");
        }
        return CategoryResponse.from(categoryRepository.save(new Category(name)));
    }
}