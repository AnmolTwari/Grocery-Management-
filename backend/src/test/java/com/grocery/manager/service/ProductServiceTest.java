package com.grocery.manager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import com.grocery.manager.dto.product.ProductRequest;
import com.grocery.manager.dto.product.ProductResponse;
import com.grocery.manager.entity.Category;
import com.grocery.manager.entity.Product;
import com.grocery.manager.entity.StockStatus;
import com.grocery.manager.entity.Unit;
import com.grocery.manager.exception.DuplicateResourceException;
import com.grocery.manager.exception.ResourceNotFoundException;
import com.grocery.manager.repository.CategoryRepository;
import com.grocery.manager.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        category = new Category("Dairy");
        ReflectionTestUtils.setField(category, "id", 5L);
        product = new Product("Milk", category, "Amul", "MILK-1", Unit.PIECE,
                new BigDecimal("20.00"), new BigDecimal("25.00"),
                new BigDecimal("10"), new BigDecimal("5"), true);
        ReflectionTestUtils.setField(product, "id", 1L);
    }

    private ProductRequest request(String name, String sku, BigDecimal currentQty,
            BigDecimal minStock) {
        return new ProductRequest(name, 5L, "Amul", sku, Unit.PIECE,
                new BigDecimal("20.00"), new BigDecimal("25.00"), currentQty, minStock, true);
    }

    @Test
    void createProductSavesAndReturnsResponse() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySkuIgnoreCase("MILK-1")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response =
                productService.createProduct(request("Milk", "MILK-1", new BigDecimal("10"),
                        new BigDecimal("5")));

        assertThat(response.name()).isEqualTo("Milk");
        assertThat(response.categoryName()).isEqualTo("Dairy");
        assertThat(response.stockStatus()).isEqualTo(StockStatus.IN_STOCK);
        assertThat(response.active()).isTrue();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProductRejectsDuplicateSku() {
        when(productRepository.existsBySkuIgnoreCase("MILK-1")).thenReturn(true);

        assertThatThrownBy(
                () -> productService.createProduct(request("Milk", "MILK-1", null, null)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createProductDefaultsMissingQuantityToZero() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(productRepository.existsBySkuIgnoreCase("MILK-1")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response =
                productService.createProduct(request("Milk", "MILK-1", null, null));

        assertThat(response.currentQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.stockStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);
    }

    @Test
    void updateProductUpdatesFields() {
        when(categoryRepository.findById(5L)).thenReturn(Optional.of(category));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.existsBySkuIgnoreCaseAndIdNot("MILK-2", 1L)).thenReturn(false);

        ProductResponse response = productService.updateProduct(1L,
                request("Full Cream Milk", "MILK-2", new BigDecimal("2"),
                        new BigDecimal("5")));

        assertThat(response.name()).isEqualTo("Full Cream Milk");
        assertThat(response.sku()).isEqualTo("MILK-2");
        assertThat(response.stockStatus()).isEqualTo(StockStatus.LOW_STOCK);
    }

    @Test
    void getProductReturnsExistingProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProduct(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Milk");
    }

    @Test
    void getProductThrowsWhenMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deactivateProductMarksItInactive() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deactivateProduct(1L);

        assertThat(product.isActive()).isFalse();
    }

    @Test
    void listProductsReturnsActivePage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        Page<ProductResponse> page =
                productService.listProducts("milk", null, null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().name()).isEqualTo("Milk");
    }

    @Test
    void stockStatusDerivation() {
        assertThat(productWithStock(new BigDecimal("0"), new BigDecimal("5")).stockStatus())
                .isEqualTo(StockStatus.OUT_OF_STOCK);
        assertThat(productWithStock(new BigDecimal("5"), new BigDecimal("5")).stockStatus())
                .isEqualTo(StockStatus.LOW_STOCK);
        assertThat(productWithStock(new BigDecimal("6"), new BigDecimal("5")).stockStatus())
                .isEqualTo(StockStatus.IN_STOCK);
    }

    private Product productWithStock(BigDecimal qty, BigDecimal min) {
        return new Product("X", category, null, null, Unit.PIECE,
                BigDecimal.ONE, BigDecimal.ONE, qty, min, true);
    }
}