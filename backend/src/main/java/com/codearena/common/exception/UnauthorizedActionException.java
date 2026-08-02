package com.codearena.common.exception;

/** Thrown when an authenticated user tries to act outside their role/ownership scope. */
public class UnauthorizedActionException extends RuntimeException {
    public UnauthorizedActionException(String message) {
        super(message);
    }
}
