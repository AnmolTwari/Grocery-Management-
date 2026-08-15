package com.shopmanager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopmanager.dto.report.ReportSummaryResponse;
import com.shopmanager.entity.User;
import com.shopmanager.repository.SaleRepository;
import com.shopmanager.security.CurrentUserService;


@Service
public class ReportService {

    private final SaleRepository saleRepository;
    private final CurrentUserService currentUserService;
    private final ExecutorService queryExecutor;

    public ReportService(SaleRepository saleRepository, CurrentUserService currentUserService,
            ExecutorService queryExecutor) {
        this.saleRepository = saleRepository;
        this.currentUserService = currentUserService;
        this.queryExecutor = queryExecutor;
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponse summary(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }
        User owner = currentUserService.currentUser();
        LocalDateTime fromTime = from.atStartOfDay();
        LocalDateTime toTime = to.plusDays(1).atStartOfDay();

        CompletableFuture<Long> count = CompletableFuture.supplyAsync(
                () -> saleRepository.countByOwnerAndCreatedAtBetween(owner, fromTime, toTime),
                queryExecutor);
        CompletableFuture<BigDecimal> total = CompletableFuture.supplyAsync(
                () -> saleRepository.sumTotalAmountBetween(owner, fromTime, toTime),
                queryExecutor);
        CompletableFuture<BigDecimal> profit = CompletableFuture.supplyAsync(
                () -> saleRepository.sumEstimatedProfitBetween(owner, fromTime, toTime),
                queryExecutor);

        CompletableFuture.allOf(count, total, profit).join();

        long countValue = count.join();
        BigDecimal totalValue = total.join();
        BigDecimal average = countValue == 0 ? BigDecimal.ZERO
                : totalValue.divide(BigDecimal.valueOf(countValue), 2, RoundingMode.HALF_UP);

        return new ReportSummaryResponse(from, to, countValue, totalValue, profit.join(), average);
    }
}