package com.expensestracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String token;
    private String type = "Bearer";
    private UUID userId;
    private String name;
    private String email;
    private BigDecimal salary;
    
    public AuthResponse(String token, UUID userId, String name, String email, BigDecimal salary) {
        this.token = token;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.salary = salary;
    }
}
