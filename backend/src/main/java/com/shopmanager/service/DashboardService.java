package com.shopmanager.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanager.dto.dashboard.DashboardSummaryResponse;
import com.shopmanager.dto.dashboard.DashboardSummaryResponse.DailyRevenuePoint;
import com.shopmanager.dto.sale.SaleSummaryResponse;
import com.shopmanager.entity.Sale;
import com.shopmanager.entity.User;
import com.shopmanager.repository.ProductRepository;
import com.shopmanager.repository.SaleRepository;
import com.shopmanager.security.CurrentUserService;


@Service
public class DashboardService {

    private static final int RECENT_SALES_LIMIT = 5;
    private static final int DAILY_REVENUE_DAYS = 7;

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;
    private final ExecutorService queryExecutor;

    public DashboardService(SaleRepository saleRepository, ProductRepository productRepository,
            CurrentUserService currentUserService, ExecutorService queryExecutor) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.currentUserService = currentUserService;
        this.queryExecutor = queryExecutor;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse summary() {
        User owner = currentUserService.currentUser();
        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime startOfYesterday = today.minusDays(1).atStartOfDay();
        LocalDateTime startOfWindow = today.minusDays(DAILY_REVENUE_DAYS - 1).atStartOfDay();

        CompletableFuture<Page<Sale>> recent = CompletableFuture.supplyAsync(() -> saleRepository
                .findByOwner(owner, PageRequest.of(0, RECENT_SALES_LIMIT,
                        Sort.by(Sort.Direction.DESC, "createdAt")
                                .and(Sort.by(Sort.Direction.DESC, "id")))),
                queryExecutor);
        CompletableFuture<Long> salesToday = CompletableFuture.supplyAsync(
                () -> saleRepository.countByOwnerAndCreatedAtGreaterThanEqual(owner, startOfToday),
                queryExecutor);
        CompletableFuture<java.math.BigDecimal> revenue = CompletableFuture.supplyAsync(
                () -> saleRepository.sumTotalAmountSince(owner, startOfToday), queryExecutor);
        CompletableFuture<java.math.BigDecimal> profit = CompletableFuture.supplyAsync(
                () -> saleRepository.sumEstimatedProfitSince(owner, startOfToday), queryExecutor);
        CompletableFuture<Long> salesYesterday = CompletableFuture.supplyAsync(
                () -> saleRepository.countByOwnerAndCreatedAtBetween(owner, startOfYesterday,
                        startOfToday),
                queryExecutor);
        CompletableFuture<java.math.BigDecimal> revenueYesterday = CompletableFuture.supplyAsync(
                () -> saleRepository.sumTotalAmountBetween(owner, startOfYesterday, startOfToday),
                queryExecutor);
        CompletableFuture<Long> products = CompletableFuture.supplyAsync(
                () -> productRepository.countByOwnerAndActiveTrue(owner), queryExecutor);
        CompletableFuture<Long> lowStock = CompletableFuture.supplyAsync(
                () -> productRepository.countLowStock(owner), queryExecutor);
        CompletableFuture<Long> outOfStock = CompletableFuture.supplyAsync(
                () -> productRepository.countOutOfStock(owner), queryExecutor);
        CompletableFuture<List<Object[]>> dailyRows = CompletableFuture.supplyAsync(
                () -> saleRepository.sumDailyRevenue(owner, startOfWindow), queryExecutor);

        CompletableFuture.allOf(recent, salesToday, revenue, profit, salesYesterday,
                revenueYesterday, products, lowStock, outOfStock, dailyRows).join();

        List<SaleSummaryResponse> recentSales = recent.join().getContent().stream()
                .map(SaleSummaryResponse::from).toList();

        return new DashboardSummaryResponse(
                salesToday.join(),
                revenue.join(),
                profit.join(),
                salesYesterday.join(),
                revenueYesterday.join(),
                products.join(),
                lowStock.join(),
                outOfStock.join(),
                dailyRevenueSeries(dailyRows.join(), startOfWindow.toLocalDate(), today),
                recentSales);
    }

    private List<DailyRevenuePoint> dailyRevenueSeries(List<Object[]> rows, LocalDate from,
            LocalDate to) {
        Map<LocalDate, BigDecimal> byDate = new HashMap<>();
        for (Object[] row : rows) {
            byDate.put(toLocalDate(row[0]), (BigDecimal) row[1]);
        }
        List<DailyRevenuePoint> series = new ArrayList<>();
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            series.add(new DailyRevenuePoint(date,
                    byDate.getOrDefault(date, BigDecimal.ZERO)));
        }
        return series;
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }
        if (value instanceof java.util.Date utilDate) {
            return utilDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }
        throw new IllegalStateException("Unexpected date type: " + value.getClass().getName());
    }
}
