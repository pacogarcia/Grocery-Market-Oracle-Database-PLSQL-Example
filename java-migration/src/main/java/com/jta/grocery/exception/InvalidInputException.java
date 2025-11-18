package com.jta.grocery.exception;

/**
 * Exception for invalid input
 * Migrated from PL/SQL invalid_input exception (-20201)
 */
public class InvalidInputException extends JtaException {
    public InvalidInputException(String message) {
        super(-20201, message);
    }

    public InvalidInputException(String message, Throwable cause) {
        super(-20201, message, cause);
    }
}
