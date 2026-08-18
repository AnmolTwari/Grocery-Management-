package com.shopmanager.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.shopmanager.dto.sale.SaleSummaryResponse;


public record DashboardSummaryResponse(
        long salesToday,
        BigDecimal revenueToday,
        BigDecimal profitToday,
        long salesYesterday,
        BigDecimal revenueYesterday,
        long totalProducts,
        long lowStockCount,
        long outOfStockCount,
        List<DailyRevenuePoint> dailyRevenue,
        List<SaleSummaryResponse> recentSales) {

    public record DailyRevenuePoint(LocalDate date, BigDecimal total) {
    }
}