package com.grocery.manager.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * An audited stock change for a product. Records the previous/new
 * quantity so every meaningful stock change is traceable.
 */
@Entity
@Table(name = "stock_movements", indexes = {
        @Index(name = "idx_stock_move_product", columnList = "product_id"),
        @Index(name = "idx_stock_move_created", columnList = "created_at")
})
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    /** Stock before this change. */
    @Column(name = "previous_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal previousQuantity;

    /** Signed change; negative when stock was reduced. */
    @Column(name = "quantity_changed", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantityChanged;

    /** Stock after this change. */
    @Column(name = "new_quantity", nullable = false, precision = 12, scale = 3)
    private BigDecimal newQuantity;

    @Column(length = 200)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected StockMovement() {
        // Required by JPA.
    }

    public StockMovement(Product product, MovementType type, BigDecimal previousQuantity,
            BigDecimal quantityChanged, BigDecimal newQuantity, String reason) {
        this.product = product;
        this.type = type;
        this.previousQuantity = previousQuantity;
        this.quantityChanged = quantityChanged;
        this.newQuantity = newQuantity;
        this.reason = reason;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public MovementType getType() {
        return type;
    }

    public BigDecimal getPreviousQuantity() {
        return previousQuantity;
    }

    public BigDecimal getQuantityChanged() {
        return quantityChanged;
    }

    public BigDecimal getNewQuantity() {
        return newQuantity;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}