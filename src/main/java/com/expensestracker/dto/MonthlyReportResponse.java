package com.expensestracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportResponse {
    
    private int year;
    private int month;
    private String monthName;
    private BigDecimal originalSalary;
    private BigDecimal totalSpent;
    private BigDecimal amountSaved;
    private BigDecimal savingsPercentage;
    private Map<String, BigDecimal> categoryBreakdown;
    private List<ExpenseResponse> expenses;
    
    public void calculateSavingsPercentage() {
        if (originalSalary != null && originalSalary.compareTo(BigDecimal.ZERO) > 0) {
            this.savingsPercentage = amountSaved
                    .multiply(BigDecimal.valueOf(100))
                    .divide(originalSalary, 2, RoundingMode.HALF_UP);
        } else {
            this.savingsPercentage = BigDecimal.ZERO;
        }
    }
}
