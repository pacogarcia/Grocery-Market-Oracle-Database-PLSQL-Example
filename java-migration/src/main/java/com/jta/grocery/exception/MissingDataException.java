package com.jta.grocery.exception;

/**
 * Exception for missing data
 * Migrated from PL/SQL missing_data exception (-20202)
 */
public class MissingDataException extends JtaException {
    public MissingDataException(String message) {
        super(-20202, message);
    }

    public MissingDataException(String message, Throwable cause) {
        super(-20202, message, cause);
    }
}
