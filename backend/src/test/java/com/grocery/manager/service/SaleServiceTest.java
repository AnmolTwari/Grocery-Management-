package com.grocery.manager.service;

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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.grocery.manager.dto.sale.SaleItemRequest;
import com.grocery.manager.dto.sale.SaleRequest;
import com.grocery.manager.dto.sale.SaleResponse;
import com.grocery.manager.dto.sale.SaleSummaryResponse;
import com.grocery.manager.entity.Category;
import com.grocery.manager.entity.MovementType;
import com.grocery.manager.entity.Product;
import com.grocery.manager.entity.Sale;
import com.grocery.manager.entity.SaleItem;
import com.grocery.manager.entity.StockMovement;
import com.grocery.manager.entity.Unit;
import com.grocery.manager.exception.InsufficientStockException;
import com.grocery.manager.exception.ResourceNotFoundException;
import com.grocery.manager.repository.ProductRepository;
import com.grocery.manager.repository.SaleRepository;
import com.grocery.manager.repository.StockMovementRepository;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private SaleService saleService;

    private Product product;

    private SaleRequest saleRequest(BigDecimal quantity) {
        return new SaleRequest(List.of(new SaleItemRequest(1L, quantity)));
    }

    @BeforeEach
    void setUp() {
        Category category = new Category("Dairy");
        product = new Product("Milk", category, "Amul", "MILK-1", Unit.PIECE,
                new BigDecimal("20.00"), new BigDecimal("25.00"),
                new BigDecimal("10"), new BigDecimal("5"), true);
        ReflectionTestUtils.setField(product, "id", 1L);
    }

    @Test
    void createSaleComputesTotalReducesStockAndRecordsMovement() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        SaleResponse response = saleService.createSale(saleRequest(new BigDecimal("3")));

        assertThat(response.totalAmount()).isEqualByComparingTo("75.00");
        assertThat(response.items()).hasSize(1);
        assertThat(response.items().getFirst().lineTotal()).isEqualByComparingTo("75.00");
        assertThat(product.getCurrentQuantity()).isEqualByComparingTo("7");
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        StockMovement movement = captor.getValue();
        assertThat(movement.getType()).isEqualTo(MovementType.SALE);
        assertThat(movement.getPreviousQuantity()).isEqualByComparingTo("10");
        assertThat(movement.getQuantityChanged()).isEqualByComparingTo("-3");
        assertThat(movement.getNewQuantity()).isEqualByComparingTo("7");
        verify(saleRepository).save(any(Sale.class));
    }

    @Test
    void createSaleThrowsWhenStockIsInsufficient() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> saleService.createSale(saleRequest(new BigDecimal("15"))))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Milk");
        verify(saleRepository, never()).save(any(Sale.class));
        verify(stockMovementRepository, never()).save(any(StockMovement.class));
    }

    @Test
    void createSaleThrowsWhenProductMissing() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        SaleRequest request = new SaleRequest(List.of(new SaleItemRequest(99L, new BigDecimal("1"))));

        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createSaleChecksCumulativeStockAcrossDuplicateLines() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        SaleRequest request = new SaleRequest(List.of(
                new SaleItemRequest(1L, new BigDecimal("4")),
                new SaleItemRequest(1L, new BigDecimal("7"))));

        assertThatThrownBy(() -> saleService.createSale(request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void createSaleMergesDuplicateLinesAndRecordsOneMovement() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));
        when(stockMovementRepository.save(any(StockMovement.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        SaleRequest request = new SaleRequest(List.of(
                new SaleItemRequest(1L, new BigDecimal("4")),
                new SaleItemRequest(1L, new BigDecimal("5"))));

        SaleResponse response = saleService.createSale(request);

        assertThat(response.items()).hasSize(2);
        assertThat(response.totalAmount()).isEqualByComparingTo("225.00");
        assertThat(product.getCurrentQuantity()).isEqualByComparingTo("1");
        ArgumentCaptor<StockMovement> captor = ArgumentCaptor.forClass(StockMovement.class);
        verify(stockMovementRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantityChanged()).isEqualByComparingTo("-9");
        assertThat(captor.getValue().getNewQuantity()).isEqualByComparingTo("1");
    }

    @Test
    void getSaleReturnsExistingSale() {
        SaleItem item = new SaleItem(product, new BigDecimal("2"),
                new BigDecimal("25.00"), new BigDecimal("20.00"));
        Sale sale = new Sale(List.of(item), new BigDecimal("50.00"));
        ReflectionTestUtils.setField(sale, "id", 3L);
        when(saleRepository.findById(3L)).thenReturn(Optional.of(sale));

        SaleResponse response = saleService.getSale(3L);

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.totalAmount()).isEqualByComparingTo("50.00");
        assertThat(response.items().getFirst().productName()).isEqualTo("Milk");
    }

    @Test
    void getSaleThrowsWhenMissing() {
        when(saleRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.getSale(9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listSalesReturnsSummaries() {
        SaleItem item = new SaleItem(product, new BigDecimal("1"),
                new BigDecimal("25.00"), new BigDecimal("20.00"));
        Sale sale = new Sale(List.of(item), new BigDecimal("25.00"));
        ReflectionTestUtils.setField(sale, "id", 4L);
        Pageable pageable = PageRequest.of(0, 20);
        when(saleRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(sale), pageable, 1));

        Page<SaleSummaryResponse> page = saleService.listSales(pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent().getFirst().id()).isEqualTo(4L);
        assertThat(page.getContent().getFirst().itemCount()).isEqualTo(1);
        assertThat(page.getContent().getFirst().totalAmount()).isEqualByComparingTo("25.00");
    }
}