package com.expensestracker.service;

import com.expensestracker.dto.MonthlyReportResponse;
import com.expensestracker.model.Expense;
import com.expensestracker.model.User;
import com.expensestracker.repository.UserRepository;
import com.expensestracker.security.UserDetailsImpl;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.awt.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Service
public class PdfService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExpenseService expenseService;

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public byte[] generateMonthlyReportPdf(Authentication authentication, int year, int month) {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            MonthlyReportResponse report = expenseService.getMonthlyReport(authentication, year, month);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, Color.BLUE);
            Paragraph title = new Paragraph("Monthly Expense Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // User Info
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("User Name: " + user.getName(), headerFont));
            document.add(new Paragraph("Email: " + user.getEmail(), normalFont));
            document.add(new Paragraph("Report Period: " + report.getMonthName() + " " + report.getYear(), headerFont));
            document.add(new Paragraph(" "));

            // Summary Section
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
            Paragraph summaryTitle = new Paragraph("Summary", sectionFont);
            summaryTitle.setSpacingAfter(10);
            document.add(summaryTitle);

            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(60);
            summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
            summaryTable.setSpacingAfter(20);

            addSummaryRow(summaryTable, "Original Salary:", CURRENCY_FORMAT.format(report.getOriginalSalary()));
            addSummaryRow(summaryTable, "Total Expenses:", CURRENCY_FORMAT.format(report.getTotalSpent()));
            addSummaryRow(summaryTable, "Amount Saved:", CURRENCY_FORMAT.format(report.getAmountSaved()));
            addSummaryRow(summaryTable, "Savings Percentage:", report.getSavingsPercentage() + "%");

            document.add(summaryTable);

            // Expense Details
            if (!report.getExpenses().isEmpty()) {
                Paragraph expensesTitle = new Paragraph("Expense Details", sectionFont);
                expensesTitle.setSpacingBefore(10);
                expensesTitle.setSpacingAfter(10);
                document.add(expensesTitle);

                PdfPTable expensesTable = new PdfPTable(3);
                expensesTable.setWidthPercentage(100);
                expensesTable.setSpacingAfter(20);

                // Header
                String[] headers = {"Date", "Description", "Amount"};
                for (String header : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                    cell.setBackgroundColor(Color.LIGHT_GRAY);
                    expensesTable.addCell(cell);
                }

                for (var expense : report.getExpenses()) {
                    expensesTable.addCell(expense.getExpenseDate().format(DATE_FORMATTER));
                    expensesTable.addCell(expense.getDescription() != null ? expense.getDescription() : "-");
                    expensesTable.addCell(CURRENCY_FORMAT.format(expense.getAmount()));
                }

                document.add(expensesTable);
            }

            // Footer
            Paragraph footer = new Paragraph("Generated on: " + java.time.LocalDate.now().format(DATE_FORMATTER), 
                    FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            footer.setSpacingBefore(20);
            document.add(footer);

            document.close();

            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }

    private void addSummaryRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
        
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        labelCell.setBorderWidth(0);
        valueCell.setBorderWidth(0);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
