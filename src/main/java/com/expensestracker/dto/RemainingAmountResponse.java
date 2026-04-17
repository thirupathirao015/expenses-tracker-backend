package com.expensestracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RemainingAmountResponse {
    
    private BigDecimal originalSalary;
    private BigDecimal totalExpenses;
    private BigDecimal remainingAmount;
}
