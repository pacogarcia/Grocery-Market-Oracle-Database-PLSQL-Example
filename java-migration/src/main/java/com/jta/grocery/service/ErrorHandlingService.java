package com.jta.grocery.service;

import com.jta.grocery.entity.JtaError;
import com.jta.grocery.repository.JtaErrorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Error Handling Service
 * Migrated from PL/SQL jta_error package (Construct 01)
 *
 * This service handles error logging and exception throwing
 * Uses REQUIRES_NEW propagation to ensure errors are logged even if parent transaction rolls back
 * (equivalent to PRAGMA autonomous_transaction in PL/SQL)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ErrorHandlingService {

    private final JtaErrorRepository jtaErrorRepository;

    /**
     * Log error to database
     * Equivalent to jta_error.log_error procedure
     * Uses autonomous transaction (REQUIRES_NEW) so error is saved even if parent transaction rolls back
     *
     * @param code Error code
     * @param message Error message
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logError(int code, String message) {
        try {
            // Show in console (development mode)
            log.error("Error logged: {} - {}", code, message);

            // Get current user
            String userName = getCurrentUser();

            // Save to database
            JtaError error = JtaError.builder()
                    .dateTime(LocalDateTime.now())
                    .userName(userName)
                    .code(String.valueOf(code))
                    .message(message)
                    .build();

            jtaErrorRepository.save(error);

        } catch (Exception e) {
            // If error logging fails, just log to console
            log.error("Failed to log error to database: {} - {}", code, message, e);
        }
    }

    /**
     * Log error from exception
     *
     * @param code Error code
     * @param message Error message
     * @param throwable The exception
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logError(int code, String message, Throwable throwable) {
        String fullMessage = message + " | Cause: " + throwable.getMessage();
        logError(code, fullMessage);
    }

    /**
     * Show trivial error in console without logging to database
     * Equivalent to jta_error.show_in_console procedure
     *
     * @param code Error code (optional)
     * @param message Error message
     */
    public void showInConsole(Integer code, String message) {
        if (code != null) {
            log.warn("Trivial error occurred: {} - {}", code, message);
        } else {
            log.warn("Trivial error occurred: {}", message);
        }
    }

    /**
     * Get current authenticated user or default
     *
     * @return Username
     */
    private String getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("Could not get current user", e);
        }
        return "SYSTEM";
    }
}
