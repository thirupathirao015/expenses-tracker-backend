package com.expensestracker.service;

import com.expensestracker.dto.*;
import com.expensestracker.model.Expense;
import com.expensestracker.model.ExpenseCategory;
import com.expensestracker.model.User;
import com.expensestracker.repository.ExpenseRepository;
import com.expensestracker.repository.UserRepository;
import com.expensestracker.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ExpenseResponse addExpense(Authentication authentication, ExpenseRequest request) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Expense expense = new Expense();
        expense.setUser(user);
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setExpenseDate(request.getExpenseDate());

        Expense savedExpense = expenseRepository.save(expense);
        return mapToExpenseResponse(savedExpense);
    }

    @Transactional(readOnly = true)
    public RemainingAmountResponse getRemainingAmount(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        BigDecimal totalExpenses = expenseRepository.sumAmountByUserId(user.getId());
        BigDecimal remainingAmount = user.getSalary().subtract(totalExpenses);

        return new RemainingAmountResponse(
                user.getSalary(),
                totalExpenses,
                remainingAmount
        );
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getUserExpenses(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return expenseRepository.findByUserIdOrderByExpenseDateDesc(userDetails.getId())
                .stream()
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getTodayExpenses(Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        return expenseRepository.findByUserIdAndExpenseDate(userDetails.getId(), LocalDate.now())
                .stream()
                .map(this::mapToExpenseResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MonthlyReportResponse getMonthlyReport(Authentication authentication, int year, int month) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        BigDecimal totalSpent = expenseRepository.sumAmountByUserIdAndDateBetween(
                user.getId(), startDate, endDate);
        BigDecimal amountSaved = user.getSalary().subtract(totalSpent);

        List<Expense> expenses = expenseRepository.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(
                user.getId(), startDate, endDate);

        List<Object[]> categoryData = expenseRepository.sumAmountByCategoryAndDateBetween(
                user.getId(), startDate, endDate);

        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();
        for (Object[] data : categoryData) {
            ExpenseCategory category = (ExpenseCategory) data[0];
            BigDecimal amount = (BigDecimal) data[1];
            categoryBreakdown.put(category.name(), amount);
        }

        MonthlyReportResponse response = new MonthlyReportResponse();
        response.setYear(year);
        response.setMonth(month);
        response.setMonthName(Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH));
        response.setOriginalSalary(user.getSalary());
        response.setTotalSpent(totalSpent);
        response.setAmountSaved(amountSaved);
        response.calculateSavingsPercentage();
        response.setCategoryBreakdown(categoryBreakdown);
        response.setExpenses(expenses.stream().map(this::mapToExpenseResponse).collect(Collectors.toList()));

        return response;
    }

    @Transactional(readOnly = true)
    public List<Expense> getMonthlyExpenses(Authentication authentication, int year, int month) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        return expenseRepository.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDesc(
                userDetails.getId(), startDate, endDate);
    }

    private ExpenseResponse mapToExpenseResponse(Expense expense) {
        return new ExpenseResponse(
                expense.getId(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDescription(),
                expense.getExpenseDate(),
                expense.getCreatedAt()
        );
    }
}
