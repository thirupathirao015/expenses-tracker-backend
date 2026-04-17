package com.expensestracker.dto;

import com.expensestracker.model.ExpenseCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseResponse {
    
    private UUID id;
    private BigDecimal amount;
    private ExpenseCategory category;
    private String description;
    private LocalDate expenseDate;
    private LocalDateTime createdAt;
}
