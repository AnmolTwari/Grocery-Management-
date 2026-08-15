package com.shopmanager.service;

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
import org.springframework.test.util.ReflectionTestUtils;

import com.shopmanager.dto.inventory.AdjustmentRequest;
import com.shopmanager.dto.inventory.StockInRequest;
import com.shopmanager.dto.inventory.StockMovementResponse;
import com.shopmanager.entity.Category;
import com.shopmanager.entity.MovementType;
import com.shopmanager.entity.Product;
import com.shopmanager.entity.StockMovement;
import com.shopmanager.entity.Unit;
import com.shopmanager.entity.User;
import com.shopmanager.exception.ResourceNotFoundException;
import com.shopmanager.repository.ProductRepository;
import com.shopmanager.repository.StockMovementRepository;
import com.shopmanager.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private InventoryService inventoryService;

    private User owner;
    private Product product;

    @BeforeEach
    void setUp() {
        owner = User.builder().username("owner").build();
        when(currentUserService.currentUser()).thenReturn(owner);
        Category category = new Category(owner, "Dairy");
        product = new Product(owner, "Milk", category, "Amul", "MILK-1", Unit.PIECE,
                new BigDecimal("20.00"), new BigDecimal("25.00"),
                new BigDecimal("10"), new BigDecimal("5"), true);
        ReflectionTestUtils.setField(product, "id", 1L);
    }

    @Test
    void stockInAddsQuantityAndRecordsMovement() {
        when(productRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(product));
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StockMovementResponse response =
                inventoryService.stockIn(new StockInRequest(1L, new BigDecimal("10"), "Purchase"));

        assertThat(response.type()).isEqualTo(MovementType.STOCK_IN);
        assertThat(response.previousQuantity()).isEqualByComparingTo("10");
        assertThat(response.quantityChanged()).isEqualByComparingTo("10");
        assertThat(response.newQuantity()).isEqualByComparingTo("20");
        assertThat(response.productName()).isEqualTo("Milk");
        assertThat(product.getCurrentQuantity()).isEqualByComparingTo("20");
        verify(stockMovementRepository).save(any(StockMovement.class));
    }

    @Test
    void stockInThrowsWhenProductMissing() {
        when(productRepository.findByIdAndOwner(99L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> inventoryService.stockIn(new StockInRequest(99L, new BigDecimal("5"), null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void adjustSetsNewQuantityAndRecordsDelta() {
        when(productRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(product));
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        StockMovementResponse response =
                inventoryService.adjust(new AdjustmentRequest(1L, new BigDecimal("4"), "Broke items"));

        assertThat(response.type()).isEqualTo(MovementType.ADJUSTMENT);
        assertThat(response.previousQuantity()).isEqualByComparingTo("10");
        assertThat(response.quantityChanged()).isEqualByComparingTo("-6");
        assertThat(response.newQuantity()).isEqualByComparingTo("4");
        assertThat(product.getCurrentQuantity()).isEqualByComparingTo("4");
    }

    @Test
    void adjustThrowsWhenProductMissing() {
        when(productRepository.findByIdAndOwner(99L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(
                () -> inventoryService.adjust(new AdjustmentRequest(99L, new BigDecimal("0"), null)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listAllMovementsWithoutFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        StockMovement movement = new StockMovement(product, MovementType.STOCK_IN,
                new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("15"), "Purchase");
        when(stockMovementRepository.findByProduct_Owner(owner, pageable))
                .thenReturn(new PageImpl<>(List.of(movement), pageable, 1));

        Page<StockMovementResponse> page = inventoryService.listMovements(null, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().newQuantity()).isEqualByComparingTo("15");
        verify(stockMovementRepository).findByProduct_Owner(owner, pageable);
    }

    @Test
    void listMovementsFiltersByProduct() {
        Pageable pageable = PageRequest.of(0, 20);
        StockMovement movement = new StockMovement(product, MovementType.ADJUSTMENT,
                new BigDecimal("10"), new BigDecimal("-2"), new BigDecimal("8"), "Damaged");
        when(stockMovementRepository.findByProduct_OwnerAndProductId(owner, 1L, pageable))
                .thenReturn(new PageImpl<>(List.of(movement), pageable, 1));

        Page<StockMovementResponse> page = inventoryService.listMovements(1L, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().type()).isEqualTo(MovementType.ADJUSTMENT);
        verify(stockMovementRepository).findByProduct_OwnerAndProductId(owner, 1L, pageable);
    }
}