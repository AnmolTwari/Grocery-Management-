package com.shopmanager.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanager.dto.dashboard.DashboardSummaryResponse;
import com.shopmanager.dto.sale.SaleSummaryResponse;
import com.shopmanager.entity.Sale;
import com.shopmanager.entity.User;
import com.shopmanager.repository.ProductRepository;
import com.shopmanager.repository.SaleRepository;
import com.shopmanager.security.CurrentUserService;


@Service
public class DashboardService {

    private static final int RECENT_SALES_LIMIT = 5;

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
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

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
        CompletableFuture<Long> products = CompletableFuture.supplyAsync(
                () -> productRepository.countByOwnerAndActiveTrue(owner), queryExecutor);
        CompletableFuture<Long> lowStock = CompletableFuture.supplyAsync(
                () -> productRepository.countLowStock(owner), queryExecutor);
        CompletableFuture<Long> outOfStock = CompletableFuture.supplyAsync(
                () -> productRepository.countOutOfStock(owner), queryExecutor);

        CompletableFuture.allOf(recent, salesToday, revenue, profit, products, lowStock,
                outOfStock).join();

        List<SaleSummaryResponse> recentSales = recent.join().getContent().stream()
                .map(SaleSummaryResponse::from).toList();

        return new DashboardSummaryResponse(
                salesToday.join(),
                revenue.join(),
                profit.join(),
                products.join(),
                lowStock.join(),
                outOfStock.join(),
                recentSales);
    }
}
