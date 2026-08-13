package com.grocery.manager.dto.sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.grocery.manager.entity.Sale;


public record SaleSummaryResponse(
        Long id,
        int itemCount,
        BigDecimal totalAmount,
        LocalDateTime createdAt) {

    public static SaleSummaryResponse from(Sale sale) {
        return new SaleSummaryResponse(
                sale.getId(),
                sale.getItems().size(),
                sale.getTotalAmount(),
                sale.getCreatedAt());
    }
}