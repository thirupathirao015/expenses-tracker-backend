package com.expensestracker.controller;

import com.expensestracker.dto.UserDTO;
import com.expensestracker.model.User;
import com.expensestracker.repository.ExpenseRepository;
import com.expensestracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Value("${ADMIN_SECRET_KEY:admin-secret-key-2024}")
    private String adminSecretKey;

    @PostMapping("/login")
    public ResponseEntity<?> adminLogin(@RequestParam String adminKey) {
        if (!adminSecretKey.equals(adminKey)) {
            return ResponseEntity.status(401).body("Invalid admin key");
        }
        return ResponseEntity.ok("Admin login successful");
    }

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers(@RequestParam String adminKey) {
        if (!adminSecretKey.equals(adminKey)) {
            return ResponseEntity.status(403).body("Error: Invalid admin key");
        }

        List<User> users = userRepository.findAllByOrderByCreatedAtDesc();
        List<UserDTO> userDTOs = users.stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getSalary(),
                        user.getCreatedAt()
                ))
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    @GetMapping("/users/{userId}/expense-count")
    public ResponseEntity<?> getUserExpenseCount(
            @RequestParam String adminKey,
            @PathVariable String userId) {
        
        if (!adminSecretKey.equals(adminKey)) {
            return ResponseEntity.status(403).body("Error: Invalid admin key");
        }

        User user = userRepository.findById(UUID.fromString(userId))
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("Error: User not found");
        }

        long count = expenseRepository.countByUserId(UUID.fromString(userId));
        return ResponseEntity.ok(count);
    }

    @DeleteMapping("/users/{userId}/expenses")
    @Transactional
    public ResponseEntity<?> deleteUserMonthExpenses(
            @RequestParam String adminKey,
            @PathVariable String userId,
            @RequestParam int year,
            @RequestParam int month) {
        
        if (!adminSecretKey.equals(adminKey)) {
            return ResponseEntity.status(403).body("Error: Invalid admin key");
        }
        
        User user = userRepository.findById(UUID.fromString(userId))
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("Error: User not found");
        }
        
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        expenseRepository.deleteByUserIdAndExpenseDateBetween(user.getId(), startDate, endDate);
        
        return ResponseEntity.ok("Deleted all expenses for user " + user.getEmail() + " for " + year + "-" + month);
    }

    @DeleteMapping("/users/{userId}")
    @Transactional
    public ResponseEntity<?> deleteUser(
            @RequestParam String adminKey,
            @PathVariable String userId) {
        
        if (!adminSecretKey.equals(adminKey)) {
            return ResponseEntity.status(403).body("Error: Invalid admin key");
        }
        
        User user = userRepository.findById(UUID.fromString(userId))
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("Error: User not found");
        }
        
        String userEmail = user.getEmail();
        
        // Delete all expenses first
        expenseRepository.deleteByUserId(user.getId());
        
        // Delete user
        userRepository.delete(user);
        
        return ResponseEntity.ok("User " + userEmail + " and all their data have been deleted successfully");
    }
}
