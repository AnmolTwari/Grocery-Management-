package com.grocery.manager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.grocery.manager.dto.category.CategoryRequest;
import com.grocery.manager.dto.category.CategoryResponse;
import com.grocery.manager.entity.Category;
import com.grocery.manager.exception.DuplicateResourceException;
import com.grocery.manager.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategorySavesAndReturnsResponse() {
        when(categoryRepository.existsByNameIgnoreCase("dairy")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryResponse response = categoryService.createCategory(new CategoryRequest("dairy"));

        assertThat(response.name()).isEqualTo("dairy");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategoryRejectsDuplicateName() {
        when(categoryRepository.existsByNameIgnoreCase("dairy")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(new CategoryRequest("dairy")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void listCategoriesReturnsSortedCategories() {
        Category dairy = new Category("Dairy");
        ReflectionTestUtils.setField(dairy, "id", 1L);
        when(categoryRepository.findAllByOrderByNameAsc()).thenReturn(List.of(dairy));

        List<CategoryResponse> categories = categoryService.listCategories();

        assertThat(categories).hasSize(1);
        assertThat(categories.getFirst().name()).isEqualTo("Dairy");
    }
}