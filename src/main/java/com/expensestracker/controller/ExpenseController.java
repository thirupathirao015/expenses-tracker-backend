package com.expensestracker.controller;

import com.expensestracker.dto.ExpenseRequest;
import com.expensestracker.dto.ExpenseResponse;
import com.expensestracker.dto.MonthlyReportResponse;
import com.expensestracker.dto.RemainingAmountResponse;
import com.expensestracker.service.ExpenseService;
import com.expensestracker.service.PdfService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@CrossOrigin(origins = "*")
public class ExpenseController {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private PdfService pdfService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(
            Authentication authentication,
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.addExpense(authentication, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/remaining")
    public ResponseEntity<RemainingAmountResponse> getRemainingAmount(Authentication authentication) {
        RemainingAmountResponse response = expenseService.getRemainingAmount(authentication);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> getUserExpenses(Authentication authentication) {
        List<ExpenseResponse> expenses = expenseService.getUserExpenses(authentication);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/today")
    public ResponseEntity<List<ExpenseResponse>> getTodayExpenses(Authentication authentication) {
        List<ExpenseResponse> expenses = expenseService.getTodayExpenses(authentication);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/monthly")
    public ResponseEntity<MonthlyReportResponse> getMonthlyReport(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month) {
        MonthlyReportResponse report = expenseService.getMonthlyReport(authentication, year, month);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/monthly/pdf")
    public ResponseEntity<byte[]> downloadMonthlyPdf(
            Authentication authentication,
            @RequestParam int year,
            @RequestParam int month) {
        
        byte[] pdfBytes = pdfService.generateMonthlyReportPdf(authentication, year, month);
        
        String filename = String.format("expense-report-%d-%02d.pdf", year, month);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseResponse> updateExpense(
            Authentication authentication,
            @PathVariable String id,
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.updateExpense(authentication, id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(
            Authentication authentication,
            @PathVariable String id) {
        expenseService.deleteExpense(authentication, id);
        return ResponseEntity.ok().build();
    }
}
