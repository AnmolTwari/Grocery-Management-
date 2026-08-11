package com.grocery.manager.entity;

/** The kind of stock change recorded in stock_movements. */
public enum MovementType {
    /** Stock was added to a product. */
    STOCK_IN,
    /** Stock quantity was manually corrected. */
    ADJUSTMENT,
    /** Stock was reduced by a sale. */
    SALE
}