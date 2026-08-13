package com.grocery.manager.dto.inventory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.grocery.manager.entity.MovementType;
import com.grocery.manager.entity.Product;
import com.grocery.manager.entity.StockMovement;
import com.grocery.manager.entity.Unit;


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
                movement.getReason(),
                movement.getCreatedAt());
    }
}