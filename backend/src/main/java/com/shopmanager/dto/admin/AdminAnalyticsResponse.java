package com.shopmanager.dto.admin;

import java.math.BigDecimal;

public record AdminAnalyticsResponse(
        long totalUsers,
        long activeUsers,
        long adminUsers,
        long totalProducts,
        long lowStockProducts,
        long outOfStockProducts,
        long totalSales,
        long salesToday,
        BigDecimal totalRevenue,
        BigDecimal revenueToday) {
}
