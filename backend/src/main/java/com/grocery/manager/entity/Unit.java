package com.grocery.manager.entity;

/**
 * Supported product units. Serialized by name (e.g. {@code KG}, {@code ML});
 * the frontend maps these to friendly labels.
 */
public enum Unit {
    PIECE,
    PACKET,
    BOX,
    BOTTLE,
    KG,
    GRAM,
    LITRE,
    ML
}