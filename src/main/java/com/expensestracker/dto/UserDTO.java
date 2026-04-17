package com.expensestracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserDTO {
    private UUID id;
    private String name;
    private String email;
    private BigDecimal salary;
    private LocalDateTime createdAt;

    public UserDTO(UUID id, String name, String email, BigDecimal salary, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.salary = salary;
        this.createdAt = createdAt;
    }

    // Getters
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public BigDecimal getSalary() { return salary; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
