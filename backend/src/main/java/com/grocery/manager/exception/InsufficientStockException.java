package com.grocery.manager.exception;

/** Thrown when a sale would reduce stock below the available quantity. Maps to HTTP 409. */
public class InsufficientStockException extends RuntimeException {

    private final String errorCode;

    public InsufficientStockException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}