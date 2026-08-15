package com.shopmanager.exception;


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