package com.jta.grocery.controller;

import com.jta.grocery.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Payroll REST API Controller
 * Exposes payroll-related operations
 */
@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
@Tag(name = "Payroll", description = "Payroll management APIs")
public class PayrollController {

    private final PayrollService payrollService;

    @PostMapping("/process")
    @Operation(summary = "Process payroll for a week", description = "Process payroll for all staff who worked during the week containing the specified date")
    public ResponseEntity<String> processPayroll(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        payrollService.processPayroll(date);
        return ResponseEntity.ok("Payroll processed successfully for week containing " + date);
    }

    @GetMapping("/hours/{staffId}")
    @Operation(summary = "Get hours breakdown for staff", description = "Get basic, overtime, and double-time hours for a staff member")
    public ResponseEntity<PayrollService.HoursBreakdown> getHours(
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        PayrollService.HoursBreakdown hours = payrollService.getHours(staffId, startDate, endDate);
        return ResponseEntity.ok(hours);
    }

    @GetMapping("/sunday-check/{staffId}")
    @Operation(summary = "Check Sunday availability", description = "Check how many Sundays a staff member has worked and if they can work another")
    public ResponseEntity<PayrollService.SundayCheckResult> sundayCheck(
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate month) {
        PayrollService.SundayCheckResult result = payrollService.sundayCheck(staffId, month);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/payout/{staffId}")
    @Operation(summary = "Get payout summary", description = "Get payout summary for a staff member for a date range")
    public ResponseEntity<PayrollService.PayoutSummary> getPayout(
            @PathVariable Long staffId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate beginDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        PayrollService.PayoutSummary payout = payrollService.getPayout(staffId, beginDate, endDate);
        return ResponseEntity.ok(payout);
    }
}
