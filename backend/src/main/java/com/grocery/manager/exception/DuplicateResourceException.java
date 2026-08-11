package com.grocery.manager.exception;

/** Thrown when a record conflicts with an existing one (e.g. duplicate name). Maps to HTTP 409. */
public class DuplicateResourceException extends RuntimeException {

    private final String errorCode;

    /** Optional field the conflict belongs to, so the client can highlight it. */
    private final String field;

    public DuplicateResourceException(String errorCode, String message, String field) {
        super(message);
        this.errorCode = errorCode;
        this.field = field;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getField() {
        return field;
    }
}