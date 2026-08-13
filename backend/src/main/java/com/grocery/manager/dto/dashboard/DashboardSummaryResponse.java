package com.grocery.manager.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

import com.grocery.manager.dto.sale.SaleSummaryResponse;

/** Aggregated shop status shown on the dashboard overview. */
public record DashboardSummaryResponse(
        long salesToday,
        BigDecimal revenueToday,
        BigDecimal profitToday,
        long totalProducts,
        long lowStockCount,
        long outOfStockCount,
        List<SaleSummaryResponse> recentSales) {
}