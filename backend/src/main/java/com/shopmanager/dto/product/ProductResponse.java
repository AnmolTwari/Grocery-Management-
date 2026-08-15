package com.shopmanager.dto.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.shopmanager.entity.Product;
import com.shopmanager.entity.StockStatus;
import com.shopmanager.entity.Unit;


public record ProductResponse(
        Long id,
        String name,
        Long categoryId,
        String categoryName,
        String brand,
        String sku,
        Unit unit,
        BigDecimal purchasePrice,
        BigDecimal sellingPrice,
        BigDecimal currentQuantity,
        BigDecimal minimumStockLevel,
        StockStatus stockStatus,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getCategory().getId(),
                product.getCategory().getName(),
                product.getBrand(),
                product.getSku(),
                product.getUnit(),
                product.getPurchasePrice(),
                product.getSellingPrice(),
                product.getCurrentQuantity(),
                product.getMinimumStockLevel(),
                product.stockStatus(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}