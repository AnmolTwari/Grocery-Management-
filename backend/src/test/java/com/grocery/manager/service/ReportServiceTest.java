package com.grocery.manager.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.grocery.manager.dto.report.ReportSummaryResponse;
import com.grocery.manager.entity.User;
import com.grocery.manager.repository.SaleRepository;
import com.grocery.manager.security.CurrentUserService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReportServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private ReportService reportService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder().username("owner").build();
        when(currentUserService.currentUser()).thenReturn(owner);
    }

    @Test
    void summaryAggregatesRangeAndAverages() {
        when(saleRepository.countByOwnerAndCreatedAtBetween(any(User.class),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(4L);
        when(saleRepository.sumTotalAmountBetween(any(User.class), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("100.00"));
        when(saleRepository.sumEstimatedProfitBetween(any(User.class), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(new BigDecimal("25.00"));

        ReportSummaryResponse report = reportService.summary(LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 13));

        assertThat(report.salesCount()).isEqualTo(4L);
        assertThat(report.totalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(report.totalProfit()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(report.averageOrderValue()).isEqualByComparingTo(new BigDecimal("25.00"));
    }

    @Test
    void summaryWithNoSalesHasZeroAverage() {
        when(saleRepository.countByOwnerAndCreatedAtBetween(any(User.class),
                any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0L);
        when(saleRepository.sumTotalAmountBetween(any(User.class), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);
        when(saleRepository.sumEstimatedProfitBetween(any(User.class), any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(BigDecimal.ZERO);

        ReportSummaryResponse report = reportService.summary(LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 13));

        assertThat(report.salesCount()).isZero();
        assertThat(report.averageOrderValue()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void summaryRejectsInvertedRange() {
        assertThatThrownBy(() -> reportService.summary(LocalDate.of(2026, 8, 13),
                LocalDate.of(2026, 8, 1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("from");
    }
}