package com.shopmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanager.dto.category.CategoryRequest;
import com.shopmanager.dto.category.CategoryResponse;
import com.shopmanager.entity.Category;
import com.shopmanager.entity.User;
import com.shopmanager.exception.DuplicateResourceException;
import com.shopmanager.repository.CategoryRepository;
import com.shopmanager.security.CurrentUserService;

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