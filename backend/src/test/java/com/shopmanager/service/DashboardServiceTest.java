package com.shopmanager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.shopmanager.dto.dashboard.DashboardSummaryResponse;
import com.shopmanager.entity.Category;
import com.shopmanager.entity.Product;
import com.shopmanager.entity.Sale;
import com.shopmanager.entity.SaleItem;
import com.shopmanager.entity.Unit;
import com.shopmanager.entity.User;
import com.shopmanager.repository.ProductRepository;
import com.shopmanager.repository.SaleRepository;
import com.shopmanager.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CurrentUserService currentUserService;

    private DashboardService dashboardService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder().username("owner").build();
        when(currentUserService.currentUser()).thenReturn(owner);
        dashboardService = new DashboardService(saleRepository, productRepository,
                currentUserService, Executors.newFixedThreadPool(4));
    }

    @Test
    void summaryCombinesTodayCountersAndRecentSales() {
        Category category = new Category(owner, "Dairy");
        Product product = new Product(owner, "Milk", category, null, "MILK-1", Unit.PIECE,
                new BigDecimal("19.00"), new BigDecimal("26.00"),
                new BigDecimal("15"), new BigDecimal("5"), true);
        Sale sale = new Sale(owner,
                List.of(new SaleItem(product, new BigDecimal("3"), new BigDecimal("26.00"),
                        new BigDecimal("19.00"))),
                new BigDecimal("78.00"));
        ReflectionTestUtils.setField(sale, "id", 2L);

        when(saleRepository.findByOwner(any(User.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sale)));
        when(saleRepository.countByOwnerAndCreatedAtGreaterThanEqual(any(User.class),
                any(LocalDateTime.class)))
                .thenReturn(1L);
        when(saleRepository.sumTotalAmountSince(any(User.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("78.00"));
        when(saleRepository.sumEstimatedProfitSince(any(User.class), any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("21.00"));
        when(productRepository.countByOwnerAndActiveTrue(any(User.class))).thenReturn(2L);
        when(productRepository.countLowStock(any(User.class))).thenReturn(1L);
        when(productRepository.countOutOfStock(any(User.class))).thenReturn(0L);

        DashboardSummaryResponse summary = dashboardService.summary();

        assertThat(summary.salesToday()).isEqualTo(1L);
        assertThat(summary.revenueToday()).isEqualByComparingTo(new BigDecimal("78.00"));
        assertThat(summary.profitToday()).isEqualByComparingTo(new BigDecimal("21.00"));
        assertThat(summary.totalProducts()).isEqualTo(2L);
        assertThat(summary.lowStockCount()).isEqualTo(1L);
        assertThat(summary.outOfStockCount()).isZero();
        assertThat(summary.recentSales()).hasSize(1);
        assertThat(summary.recentSales().getFirst().totalAmount())
                .isEqualByComparingTo(new BigDecimal("78.00"));
    }

    @Test
    void summarySurvivesEmptyDay() {
        when(saleRepository.findByOwner(any(User.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        when(saleRepository.countByOwnerAndCreatedAtGreaterThanEqual(any(User.class),
                any(LocalDateTime.class)))
                .thenReturn(0L);
        when(saleRepository.sumTotalAmountSince(any(User.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(saleRepository.sumEstimatedProfitSince(any(User.class), any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(productRepository.countByOwnerAndActiveTrue(any(User.class))).thenReturn(0L);
        when(productRepository.countLowStock(any(User.class))).thenReturn(0L);
        when(productRepository.countOutOfStock(any(User.class))).thenReturn(0L);

        DashboardSummaryResponse summary = dashboardService.summary();

        assertThat(summary.salesToday()).isZero();
        assertThat(summary.revenueToday()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.recentSales()).isEmpty();
    }
}

