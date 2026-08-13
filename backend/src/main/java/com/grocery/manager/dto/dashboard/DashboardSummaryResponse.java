package com.grocery.manager.dto.dashboard;

import java.math.BigDecimal;
import java.util.List;

import com.grocery.manager.dto.sale.SaleSummaryResponse;


public record DashboardSummaryResponse(
        long salesToday,
        BigDecimal revenueToday,
        BigDecimal profitToday,
        long totalProducts,
        long lowStockCount,
        long outOfStockCount,
        List<SaleSummaryResponse> recentSales) {
}