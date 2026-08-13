package com.grocery.manager.dto.sale;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.grocery.manager.entity.Sale;


public record SaleResponse(
        Long id,
        BigDecimal totalAmount,
        List<SaleItemResponse> items,
        LocalDateTime createdAt) {

    public static SaleResponse from(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getTotalAmount(),
                sale.getItems().stream().map(SaleItemResponse::from).toList(),
                sale.getCreatedAt());
    }
}