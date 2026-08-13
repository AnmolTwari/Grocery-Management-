package com.grocery.manager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grocery.manager.dto.category.CategoryRequest;
import com.grocery.manager.dto.category.CategoryResponse;
import com.grocery.manager.entity.Category;
import com.grocery.manager.entity.User;
import com.grocery.manager.exception.DuplicateResourceException;
import com.grocery.manager.repository.CategoryRepository;
import com.grocery.manager.security.CurrentUserService;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CurrentUserService currentUserService;

    public CategoryService(CategoryRepository categoryRepository,
            CurrentUserService currentUserService) {
        this.categoryRepository = categoryRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> listCategories() {
        return categoryRepository.findByOwnerOrderByNameAsc(currentUserService.currentUser())
                .stream()
                .map(CategoryResponse::from)
                .toList();
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        User owner = currentUserService.currentUser();
        String name = request.name().trim();
        if (categoryRepository.existsByOwnerAndNameIgnoreCase(owner, name)) {
            throw new DuplicateResourceException("CATEGORY_EXISTS",
                    "Category '" + name + "' already exists", "name");
        }
        return CategoryResponse.from(categoryRepository.save(new Category(owner, name)));
    }
}