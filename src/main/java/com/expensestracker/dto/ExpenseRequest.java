package com.expensestracker.dto;

import com.expensestracker.model.ExpenseCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseRequest {
    
    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @NotNull(message = "Category is required")
    private ExpenseCategory category;
    
    private String description;
    
    @NotNull(message = "Expense date is required")
    private LocalDate expenseDate;
}
