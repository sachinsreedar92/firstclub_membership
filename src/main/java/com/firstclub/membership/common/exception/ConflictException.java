package com.firstclub.membership.common.exception;

/** Thrown on a business rule / state conflict (e.g., duplicate active subscription). Maps to HTTP 409. */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
