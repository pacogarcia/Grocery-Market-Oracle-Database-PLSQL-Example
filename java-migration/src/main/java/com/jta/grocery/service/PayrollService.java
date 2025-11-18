package com.jta.grocery.service;

import com.jta.grocery.entity.Payroll;
import com.jta.grocery.entity.Staff;
import com.jta.grocery.entity.WorkHours;
import com.jta.grocery.exception.InvalidInputException;
import com.jta.grocery.repository.PayrollRepository;
import com.jta.grocery.repository.StaffRepository;
import com.jta.grocery.repository.WorkHoursRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Payroll Service
 * Migrated from PL/SQL jta package payroll functions (Constructs 02, 03, 21, 22)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PayrollService {

    private final PayrollRepository payrollRepository;
    private final WorkHoursRepository workHoursRepository;
    private final StaffRepository staffRepository;
    private final ErrorHandlingService errorHandlingService;

    @Value("${jta.payroll.nat-insurance-rate:0.132}")
    private BigDecimal natInsuranceRate;

    @Value("${jta.payroll.hlt-surcharge-rate:0.005}")
    private BigDecimal hltSurchargeRate;

    /**
     * Get hours worked for a staff member
     * Migrated from PL/SQL get_hours procedure (Construct 02)
     *
     * @param staffId Staff ID
     * @param startDate Start date
     * @param endDate End date
     * @return Hours breakdown
     */
    public HoursBreakdown getHours(Long staffId, LocalDate startDate, LocalDate endDate) {
        int basic = 0;
        int overtime = 0;
        int doubletime = 0;

        List<WorkHours> workHoursList = workHoursRepository.findByStaffIdAndDateRange(
                staffId, startDate, endDate);

        for (WorkHours wh : workHoursList) {
            // Sunday is double time (day 7 in Java)
            if (wh.getWorkDate().getDayOfWeek() == DayOfWeek.SUNDAY) {
                doubletime += wh.getHoursWorked();
            } else {
                basic += wh.getHoursWorked();
            }
        }

        // Calculate overtime (hours over 40)
        if (basic > 40) {
            overtime = basic - 40;
            basic = 40;
        }

        return new HoursBreakdown(basic, overtime, doubletime);
    }

    /**
     * Process payroll for a given week
     * Migrated from PL/SQL process_payroll procedure (Construct 03)
     *
     * @param date Any date in the week to process
     */
    @Transactional
    public void processPayroll(LocalDate date) {
        try {
            // Calculate week start (Sunday) and end (Saturday 23:59:59)
            LocalDate startDate = date.with(DayOfWeek.SUNDAY);
            LocalDate endDate = startDate.plusDays(6);

            log.debug("Processing payroll for week: {} to {}", startDate, endDate);

            // Delete existing payroll records for this week
            payrollRepository.deleteByStartDateAndEndDate(startDate, endDate);

            // Get distinct staff IDs who worked this week
            List<Long> staffIds = workHoursRepository.findDistinctStaffIdsByDateRange(startDate, endDate);

            if (staffIds.isEmpty()) {
                throw new InvalidInputException(
                        "There was no work hours recorded for this week: " + startDate + " to " + endDate);
            }

            int count = 0;
            for (Long staffId : staffIds) {
                Staff staff = staffRepository.findById(staffId)
                        .orElseThrow(() -> new InvalidInputException("Staff not found: " + staffId));

                // Get hours worked
                HoursBreakdown hours = getHours(staffId, startDate, endDate);

                // Calculate gross pay
                BigDecimal wageRate = staff.getWageRate();
                BigDecimal grossPay = calculateGrossPay(hours, wageRate);

                // Calculate deductions
                BigDecimal natInsuranceDeduction = grossPay.multiply(natInsuranceRate)
                        .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
                BigDecimal hltSurchargeDeduction = grossPay.multiply(hltSurchargeRate)
                        .setScale(2, RoundingMode.HALF_UP);

                // Calculate net pay
                BigDecimal netPay = grossPay.subtract(natInsuranceDeduction).subtract(hltSurchargeDeduction);

                // Create payroll record
                Payroll payroll = Payroll.builder()
                        .staff(staff)
                        .startDate(startDate)
                        .endDate(endDate)
                        .hoursBasic(hours.getBasic())
                        .hoursOvertime(hours.getOvertime())
                        .hoursDoubletime(hours.getDoubletime())
                        .basicPayRate(wageRate)
                        .grossPay(grossPay)
                        .natInsuranceDeduction(natInsuranceDeduction)
                        .hltSurchargeDeduction(hltSurchargeDeduction)
                        .netPay(netPay)
                        .build();

                payrollRepository.save(payroll);
                count++;
            }

            log.info("Processed payroll for {} staff members", count);

        } catch (InvalidInputException e) {
            errorHandlingService.showInConsole(e.getErrorCode(), e.getMessage());
            throw e;
        } catch (Exception e) {
            errorHandlingService.logError(-20000, "Error processing payroll: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Check Sunday availability for staff
     * Migrated from PL/SQL sunday_check procedure (Construct 21)
     *
     * @param staffId Staff ID
     * @param month Any date in the month
     * @return Sunday check result
     */
    public SundayCheckResult sundayCheck(Long staffId, LocalDate month) {
        try {
            LocalDate monthStart = month.withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            List<WorkHours> workDays = workHoursRepository.findByStaffIdAndDateRange(
                    staffId, monthStart, monthEnd);

            int sundays = 0;
            for (WorkHours wh : workDays) {
                if (wh.getWorkDate().getDayOfWeek() == DayOfWeek.SUNDAY) {
                    sundays++;
                }
            }

            // Employees can only work 2 Sundays per month
            boolean available = sundays < 2;

            return new SundayCheckResult(sundays, available);

        } catch (Exception e) {
            errorHandlingService.logError(-20000, "Error in sunday check: " + e.getMessage(), e);
            return new SundayCheckResult(null, null);
        }
    }

    /**
     * Get payout summary for staff
     * Migrated from PL/SQL payout procedure (Construct 22)
     *
     * @param staffId Staff ID
     * @param beginDate Begin date
     * @param endDate End date
     * @return Payout summary
     */
    public PayoutSummary getPayout(Long staffId, LocalDate beginDate, LocalDate endDate) {
        try {
            List<Payroll> payrolls = payrollRepository.findByStaffAndDateRange(staffId, beginDate, endDate);

            BigDecimal grossPay = BigDecimal.ZERO;
            BigDecimal netPay = BigDecimal.ZERO;
            BigDecimal hlt = BigDecimal.ZERO;
            BigDecimal nat = BigDecimal.ZERO;

            for (Payroll p : payrolls) {
                grossPay = grossPay.add(p.getGrossPay() != null ? p.getGrossPay() : BigDecimal.ZERO);
                netPay = netPay.add(p.getNetPay() != null ? p.getNetPay() : BigDecimal.ZERO);
                hlt = hlt.add(p.getHltSurchargeDeduction() != null ? p.getHltSurchargeDeduction() : BigDecimal.ZERO);
                nat = nat.add(p.getNatInsuranceDeduction() != null ? p.getNatInsuranceDeduction() : BigDecimal.ZERO);
            }

            BigDecimal deductions = hlt.add(nat);

            return new PayoutSummary(grossPay, netPay, hlt, nat, deductions);

        } catch (Exception e) {
            errorHandlingService.logError(-20000, "Error getting payout: " + e.getMessage(), e);
            return new PayoutSummary(null, null, null, null, null);
        }
    }

    /**
     * Calculate gross pay from hours and wage rate
     *
     * @param hours Hours breakdown
     * @param wageRate Wage rate
     * @return Gross pay
     */
    private BigDecimal calculateGrossPay(HoursBreakdown hours, BigDecimal wageRate) {
        BigDecimal basic = wageRate.multiply(BigDecimal.valueOf(hours.getBasic()));
        BigDecimal overtime = wageRate.multiply(BigDecimal.valueOf(hours.getOvertime()))
                .multiply(BigDecimal.valueOf(1.5));
        BigDecimal doubletime = wageRate.multiply(BigDecimal.valueOf(hours.getDoubletime()))
                .multiply(BigDecimal.valueOf(2));

        return basic.add(overtime).add(doubletime);
    }

    // DTOs
    @Data
    public static class HoursBreakdown {
        private final int basic;
        private final int overtime;
        private final int doubletime;
    }

    @Data
    public static class SundayCheckResult {
        private final Integer sundays;
        private final Boolean available;
    }

    @Data
    public static class PayoutSummary {
        private final BigDecimal grossPay;
        private final BigDecimal netPay;
        private final BigDecimal hlt;
        private final BigDecimal nat;
        private final BigDecimal deductions;
    }
}
