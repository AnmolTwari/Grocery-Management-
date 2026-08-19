package com.shopmanager.dto.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.shopmanager.entity.MovementType;
import com.shopmanager.entity.Product;
import com.shopmanager.entity.StockMovement;
import com.shopmanager.entity.StockStatus;
import com.shopmanager.entity.Unit;


public record StockMovementResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        Unit unit,
        MovementType type,
        BigDecimal previousQuantity,
        BigDecimal quantityChanged,
        BigDecimal newQuantity,
        StockStatus stockStatus,
        String reason,
        LocalDateTime createdAt) {

    public static StockMovementResponse from(StockMovement movement) {
        Product product = movement.getProduct();
        return new StockMovementResponse(
                movement.getId(),
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getUnit(),
                movement.getType(),
                movement.getPreviousQuantity(),
                movement.getQuantityChanged(),
                movement.getNewQuantity(),
                statusFor(movement.getNewQuantity(), product.getMinimumStockLevel()),
                movement.getReason(),
                movement.getCreatedAt());
    }

    private static StockStatus statusFor(BigDecimal quantity, BigDecimal minimumStockLevel) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return StockStatus.OUT_OF_STOCK;
        }
        if (quantity.compareTo(minimumStockLevel) <= 0) {
            return StockStatus.LOW_STOCK;
        }
        return StockStatus.IN_STOCK;
    }
}