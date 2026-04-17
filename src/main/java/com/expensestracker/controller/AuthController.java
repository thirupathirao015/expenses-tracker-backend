package com.expensestracker.controller;

import com.expensestracker.dto.AuthResponse;
import com.expensestracker.dto.LoginRequest;
import com.expensestracker.dto.RegisterRequest;
import com.expensestracker.model.User;
import com.expensestracker.repository.ExpenseRepository;
import com.expensestracker.repository.UserRepository;
import com.expensestracker.security.JwtUtil;
import com.expensestracker.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Value("${admin.secret-key:admin-secret-key-2024}")
    private String adminSecretKey;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Error: Email is already taken!");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setSalary(request.getSalary());

        userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getEmail(),
                user.getSalary()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String jwt = jwtUtil.generateToken(userDetails);

        User user = userRepository.findByEmail(userDetails.getEmail()).orElseThrow();

        return ResponseEntity.ok(new AuthResponse(
                jwt,
                userDetails.getId(),
                userDetails.getName(),
                userDetails.getEmail(),
                user.getSalary()
        ));
    }

    @PostMapping("/admin/reset-password")
    public ResponseEntity<?> resetPassword(
            @RequestParam String adminKey,
            @RequestParam String userEmail,
            @RequestParam String newPassword) {
        
        // Verify admin key
        if (!adminSecretKey.equals(adminKey)) {
            return ResponseEntity.status(403).body("Error: Invalid admin key");
        }
        
        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("Error: User not found with email: " + userEmail);
        }
        
        // Update password with bcrypt encryption
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return ResponseEntity.ok("Password reset successfully for user: " + userEmail);
    }

    @PutMapping("/update-salary")
    public ResponseEntity<?> updateSalary(
            Authentication authentication,
            @RequestParam BigDecimal newSalary,
            @RequestParam(required = false) String reason) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setSalary(newSalary);
        userRepository.save(user);
        
        String message = "Salary updated successfully to " + newSalary;
        if (reason != null && !reason.isEmpty()) {
            message += " (Reason: " + reason + ")";
        }
        
        return ResponseEntity.ok(message);
    }

    // ==================== ADMIN ENDPOINTS ====================

    @DeleteMapping("/admin/delete-user-month-expenses")
    @Transactional
    public ResponseEntity<?> deleteUserMonthExpenses(
            @RequestParam String adminKey,
            @RequestParam String userEmail,
            @RequestParam int year,
            @RequestParam int month) {
        
        // Verify admin key
        if (!adminSecretKey.equals(adminKey)) {
            return ResponseEntity.status(403).body("Error: Invalid admin key");
        }
        
        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("Error: User not found with email: " + userEmail);
        }
        
        // Calculate month date range
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        
        // Delete expenses for that month
        expenseRepository.deleteByUserIdAndExpenseDateBetween(user.getId(), startDate, endDate);
        
        return ResponseEntity.ok("Deleted all expenses for " + userEmail + " for " + year + "-" + month);
    }

    @DeleteMapping("/admin/delete-user")
    @Transactional
    public ResponseEntity<?> deleteUser(
            @RequestParam String adminKey,
            @RequestParam String userEmail) {
        
        // Verify admin key
        if (!adminSecretKey.equals(adminKey)) {
            return ResponseEntity.status(403).body("Error: Invalid admin key");
        }
        
        // Find user by email
        User user = userRepository.findByEmail(userEmail)
                .orElse(null);
        
        if (user == null) {
            return ResponseEntity.badRequest().body("Error: User not found with email: " + userEmail);
        }
        
        // Delete all user expenses first (cascade)
        expenseRepository.deleteByUserId(user.getId());
        
        // Delete user
        userRepository.delete(user);
        
        return ResponseEntity.ok("User " + userEmail + " and all their data have been deleted successfully");
    }
}
