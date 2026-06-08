package com.firstclub.membership.common.exception;

/** Thrown when input violates a business invariant. Maps to HTTP 400. */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
