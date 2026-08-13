package com.grocery.manager.dto.report;

import java.math.BigDecimal;
import java.time.LocalDate;


public record ReportSummaryResponse(
        LocalDate from,
        LocalDate to,
        long salesCount,
        BigDecimal totalAmount,
        BigDecimal totalProfit,
        BigDecimal averageOrderValue) {
}