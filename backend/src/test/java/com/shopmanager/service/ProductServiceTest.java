package com.shopmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import com.shopmanager.dto.common.PageResponse;
import com.shopmanager.dto.product.ProductRequest;
import com.shopmanager.dto.product.ProductResponse;
import com.shopmanager.entity.Category;
import com.shopmanager.entity.Product;
import com.shopmanager.entity.StockStatus;
import com.shopmanager.entity.Unit;
import com.shopmanager.entity.User;
import com.shopmanager.exception.DuplicateResourceException;
import com.shopmanager.exception.ResourceNotFoundException;
import com.shopmanager.repository.CategoryRepository;
import com.shopmanager.repository.ProductRepository;
import com.shopmanager.repository.SaleRepository;
import com.shopmanager.repository.StockMovementRepository;
import com.shopmanager.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ProductService productService;

    private User owner;
    private Category category;
    private Product product;

    @BeforeEach
    void setUp() {
        owner = User.builder().username("owner").build();
        when(currentUserService.currentUser()).thenReturn(owner);
        category = new Category(owner, "Dairy");
        ReflectionTestUtils.setField(category, "id", 5L);
        product = new Product(owner, "Milk", category, "Amul", "MILK-1", Unit.PIECE,
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
    void listPopularProductsReturnsTopSoldMapped() {
        Product other = new Product(owner, "Bread", category, "Britannia", "BRD-1", Unit.PIECE,
                new BigDecimal("15.00"), new BigDecimal("20.00"),
                new BigDecimal("20"), new BigDecimal("5"), true);
        ReflectionTestUtils.setField(other, "id", 2L);
        when(saleRepository.findTopSoldProducts(owner, PageRequest.of(0, 8)))
                .thenReturn(java.util.List.of(product, other));

        List<ProductResponse> popular = productService.listPopularProducts(8);

        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).name()).isEqualTo("Milk");
        assertThat(popular.get(1).name()).isEqualTo("Bread");
        verify(saleRepository).findTopSoldProducts(owner, PageRequest.of(0, 8));
    }

    @Test
    void listPopularProductsCapsLimit() {
        productService.listPopularProducts(999);

        verify(saleRepository).findTopSoldProducts(owner, PageRequest.of(0, 20));
    }

    @Test
    void createProductSavesAndReturnsResponse() {
        when(categoryRepository.findByIdAndOwner(5L, owner)).thenReturn(Optional.of(category));
        when(productRepository.existsByOwnerAndSkuIgnoreCase(owner, "MILK-1")).thenReturn(false);
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
        when(productRepository.existsByOwnerAndSkuIgnoreCase(owner, "MILK-1")).thenReturn(true);

        assertThatThrownBy(
                () -> productService.createProduct(request("Milk", "MILK-1", null, null)))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createProductDefaultsMissingQuantityToZero() {
        when(categoryRepository.findByIdAndOwner(5L, owner)).thenReturn(Optional.of(category));
        when(productRepository.existsByOwnerAndSkuIgnoreCase(owner, "MILK-1")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        ProductResponse response =
                productService.createProduct(request("Milk", "MILK-1", null, null));

        assertThat(response.currentQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.stockStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);
    }

    @Test
    void updateProductUpdatesFields() {
        when(categoryRepository.findByIdAndOwner(5L, owner)).thenReturn(Optional.of(category));
        when(productRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(product));
        when(productRepository.existsByOwnerAndSkuIgnoreCaseAndIdNot(owner, "MILK-2", 1L))
                .thenReturn(false);

        ProductResponse response = productService.updateProduct(1L,
                request("Full Cream Milk", "MILK-2", new BigDecimal("2"),
                        new BigDecimal("5")));

        assertThat(response.name()).isEqualTo("Full Cream Milk");
        assertThat(response.sku()).isEqualTo("MILK-2");
        assertThat(response.stockStatus()).isEqualTo(StockStatus.LOW_STOCK);
    }

    @Test
    void getProductReturnsExistingProduct() {
        when(productRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProduct(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Milk");
    }

    @Test
    void getProductThrowsWhenMissing() {
        when(productRepository.findByIdAndOwner(99L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProductRemovesRowWhenUnreferenced() {
        when(productRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(product));
        when(stockMovementRepository.existsByProduct_OwnerAndProductId(owner, 1L))
                .thenReturn(false);
        when(saleRepository.existsByOwnerAndItems_Product_Id(owner, 1L)).thenReturn(false);

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteProductDeactivatesWhenItHasHistory() {
        when(productRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(product));
        when(stockMovementRepository.existsByProduct_OwnerAndProductId(owner, 1L))
                .thenReturn(true);

        productService.deleteProduct(1L);

        assertThat(product.isActive()).isFalse();
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void listProductsReturnsActivePage() {
        Pageable pageable = PageRequest.of(0, 20);
        when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        PageResponse<ProductResponse> page =
                productService.listProducts("milk", null, null, pageable);

        assertThat(page.totalElements()).isEqualTo(1);
        assertThat(page.content().getFirst().name()).isEqualTo("Milk");
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
        return new Product(owner, "X", category, null, null, Unit.PIECE,
                BigDecimal.ONE, BigDecimal.ONE, qty, min, true);
    }
}