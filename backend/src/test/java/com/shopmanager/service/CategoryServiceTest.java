package com.shopmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.shopmanager.dto.category.CategoryRequest;
import com.shopmanager.dto.category.CategoryResponse;
import com.shopmanager.entity.Category;
import com.shopmanager.entity.User;
import com.shopmanager.exception.DuplicateResourceException;
import com.shopmanager.repository.CategoryRepository;
import com.shopmanager.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private CategoryService categoryService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder().username("owner").build();
        when(currentUserService.currentUser()).thenReturn(owner);
    }

    @Test
    void createCategorySavesAndReturnsResponse() {
        when(categoryRepository.existsByOwnerAndNameIgnoreCase(owner, "dairy")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        CategoryResponse response = categoryService.createCategory(new CategoryRequest("dairy"));

        assertThat(response.name()).isEqualTo("dairy");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategoryRejectsDuplicateName() {
        when(categoryRepository.existsByOwnerAndNameIgnoreCase(owner, "dairy")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(new CategoryRequest("dairy")))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void listCategoriesReturnsSortedCategories() {
        Category dairy = new Category(owner, "Dairy");
        ReflectionTestUtils.setField(dairy, "id", 1L);
        when(categoryRepository.findByOwnerOrderByNameAsc(owner)).thenReturn(List.of(dairy));

        List<CategoryResponse> categories = categoryService.listCategories();

        assertThat(categories).hasSize(1);
        assertThat(categories.getFirst().name()).isEqualTo("Dairy");
    }
}