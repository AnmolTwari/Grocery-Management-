package com.grocery.manager.entity;

/**
 * Derived stock status of a product, based on current quantity versus the
 * minimum stock level. Not stored in the database - computed on the fly.
 */
public enum StockStatus {
    IN_STOCK,
    LOW_STOCK,
    OUT_OF_STOCK
}