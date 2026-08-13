package com.grocery.manager.exception;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(long minutes) {
        super("Too many attempts. Please try again in " + minutes + " minutes.");
    }
}