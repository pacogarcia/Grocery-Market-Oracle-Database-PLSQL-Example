package com.jta.grocery.exception;

import lombok.Getter;

/**
 * Base exception class for JTA application
 * Migrated from PL/SQL jta_error package
 */
@Getter
public class JtaException extends RuntimeException {
    private final int errorCode;

    public JtaException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public JtaException(int errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
