package com.grocery.manager.dto.product;

import java.math.BigDecimal;

import com.grocery.manager.entity.Unit;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;


public record ProductRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 150, message = "Product name must be at most 150 characters")
        String name,

        @NotNull(message = "Category is required")
        Long categoryId,

        @Size(max = 100, message = "Brand must be at most 100 characters")
        String brand,

        @Size(max = 50, message = "SKU must be at most 50 characters")
        String sku,

        @NotNull(message = "Unit is required")
        Unit unit,

        @NotNull(message = "Purchase price is required")
        @DecimalMin(value = "0.00", message = "Purchase price cannot be negative")
        BigDecimal purchasePrice,

        @NotNull(message = "Selling price is required")
        @DecimalMin(value = "0.00", message = "Selling price cannot be negative")
        BigDecimal sellingPrice,

        @DecimalMin(value = "0.00", message = "Current quantity cannot be negative")
        BigDecimal currentQuantity,

        @DecimalMin(value = "0.00", message = "Minimum stock cannot be negative")
        BigDecimal minimumStockLevel,

        Boolean active) {

    public ProductRequest {
        if (name != null) {
            name = name.trim();
        }
        if (brand != null && brand.isBlank()) {
            brand = null;
        }
        if (sku != null && sku.isBlank()) {
            sku = null;
        }
    }
}