package com.grocery.manager.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * A line inside a sale. Prices are snapshotted at sale time so later
 * price changes on the product do not change historical records.
 */
@Entity
@Table(name = "sale_items", indexes = {
        @Index(name = "idx_sale_item_sale", columnList = "sale_id"),
        @Index(name = "idx_sale_item_product", columnList = "product_id")
})
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantity;

    /** Selling price charged at the time of sale. */
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    /** Purchase price snapshot, used for estimated profit. */
    @Column(name = "purchase_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal purchasePrice;

    protected SaleItem() {
        // Required by JPA.
    }

    public SaleItem(Product product, BigDecimal quantity, BigDecimal unitPrice,
            BigDecimal purchasePrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.purchasePrice = purchasePrice;
    }

    public Long getId() {
        return id;
    }

    public Sale getSale() {
        return sale;
    }

    void setSale(Sale sale) {
        this.sale = sale;
    }

    public Product getProduct() {
        return product;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getPurchasePrice() {
        return purchasePrice;
    }
}