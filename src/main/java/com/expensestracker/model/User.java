package com.expensestracker.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal salary;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "must_change_password")
    private Boolean mustChangePassword = false;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Expense> expenses = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (mustChangePassword == null) {
            mustChangePassword = false;
        }
    }
    
    // Getter that handles null values for existing records
    public Boolean getMustChangePassword() {
        return mustChangePassword != null ? mustChangePassword : false;
    }
}
