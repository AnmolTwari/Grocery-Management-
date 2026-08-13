package com.grocery.manager.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.grocery.manager.dto.report.ReportSummaryResponse;
import com.grocery.manager.entity.User;
import com.grocery.manager.repository.SaleRepository;
import com.grocery.manager.security.CurrentUserService;

/** Read-only aggregates for reports over a date range. */
@Service
public class ReportService {

    private final SaleRepository saleRepository;
    private final CurrentUserService currentUserService;

    public ReportService(SaleRepository saleRepository, CurrentUserService currentUserService) {
        this.saleRepository = saleRepository;
        this.currentUserService = currentUserService;
    }

    @Transactional(readOnly = true)
    public ReportSummaryResponse summary(LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }
        User owner = currentUserService.currentUser();
        LocalDateTime fromTime = from.atStartOfDay();
        LocalDateTime toTime = to.plusDays(1).atStartOfDay();

        long count = saleRepository.countByOwnerAndCreatedAtBetween(owner, fromTime, toTime);
        BigDecimal total = saleRepository.sumTotalAmountBetween(owner, fromTime, toTime);
        BigDecimal profit = saleRepository.sumEstimatedProfitBetween(owner, fromTime, toTime);
        BigDecimal average = count == 0 ? BigDecimal.ZERO
                : total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP);

        return new ReportSummaryResponse(from, to, count, total, profit, average);
    }
}