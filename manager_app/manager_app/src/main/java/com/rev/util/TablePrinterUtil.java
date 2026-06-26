package com.rev.util;

import java.util.ArrayList;
import java.util.List;

import com.rev.dao.dto.EmployeeReportDTO;
import com.rev.dao.dto.ExpenseWithStatusDTO;
import com.rev.dao.model.Expense;


public class TablePrinterUtil {

    public static void printExpenses(List<Expense> expenses) {

        List<Object[]> rows = new ArrayList<>();

        for (Expense e : expenses) {
            rows.add(new Object[]{
                    e.getId(),
                    e.getUserId(),
                    String.format("$%.2f", e.getAmount()),
                    e.getCategory(),
                    e.getDescription(),
                    e.getDate()
            });
        }

        print(
                new String[]{"ID", "User", "Amount", "Category", "Description", "Date"},
                rows
        );
    }

    public static void printPendingExpenses(List<ExpenseWithStatusDTO> expenses) {

        List<Object[]> rows = new ArrayList<>();

        for (ExpenseWithStatusDTO dto : expenses) {
            Expense e = dto.getExpense();

            rows.add(new Object[]{
                    e.getId(),
                    e.getUserId(),
                    String.format("$%.2f", e.getAmount()),
                    e.getCategory(),
                    e.getDescription(),
                    e.getDate(),
                    dto.getStatus(),
                    dto.getComment()
            });
        }

        print(
                new String[]{"ID", "Employee ID", "Amount", "Category", "Description", "Date", "Status", "Comment"},
                rows
        );
    }

    public static void printEmployeeReport(EmployeeReportDTO report) {

        List<Object[]> rows = new ArrayList<>();

        rows.add(new Object[]{
            report.getEmployeeId(),
            String.format("$%.2f", report.getTotal()),
            String.format("$%.2f", report.getAverage()),
            report.getCount()
        });

        print(
                new String[]{"Employee ID", "Total Amount", "Average Expense", "Expense Count"},
                rows
        );

    }

    public static void printAllEmployeeReports(List<EmployeeReportDTO> report) {

        List<Object[]> rows = new ArrayList<>();

        for (EmployeeReportDTO empR : report) { 
            rows.add(new Object[]{
                empR.getEmployeeId(),
                String.format("$%.2f", empR.getTotal()),
                String.format("$%.2f", empR.getAverage()),
                empR.getCount()
            });
        }

        print(
                new String[]{"Employee ID", "Total Amount", "Average Expense", "Expense Count"},
                rows
        );

    }

    public static void print(String[] headers, List<Object[]> rows) {
        int cols = headers.length;
        int[] widths = new int[cols];

        // Determine column widths
        for (int i = 0; i < cols; i++) {
            widths[i] = headers[i].length();
        }

        for (Object[] row : rows) {
            for (int i = 0; i < cols; i++) {
                String value = row[i] == null ? "" : row[i].toString();
                widths[i] = Math.max(widths[i], value.length());
            }
        }

        printBorder(widths);
        printRow(headers, widths);
        printBorder(widths);

        for (Object[] row : rows) {
            String[] values = new String[cols];
            for (int i = 0; i < cols; i++) {
                values[i] = row[i] == null ? "" : row[i].toString();
            }
            printRow(values, widths);
        }

        printBorder(widths);
    }

    private static void printBorder(int[] widths) {
        System.out.print("+");
        for (int width : widths) {
            System.out.print("-".repeat(width + 2));
            System.out.print("+");
        }
        System.out.println();
    }

    private static void printRow(String[] values, int[] widths) {
        System.out.print("|");
        for (int i = 0; i < values.length; i++) {
            System.out.printf(" %-" + widths[i] + "s |", values[i]);
        }
        System.out.println();
    }
}
