package com.quantalabs.jamusync.controller;

import com.quantalabs.jamusync.JamuSyncApp;
import com.quantalabs.jamusync.dao.TransactionDAO;
import com.quantalabs.jamusync.dao.TransactionItemDAO;
import com.quantalabs.jamusync.model.Transaction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

public class ReportController {

    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Label revenueLabel;
    @FXML private Label expensesLabel;
    @FXML private Label profitLabel;
    @FXML private Label transactionCountLabel;
    @FXML private Label messageLabel;

    private final TransactionDAO transactionDAO = new TransactionDAO();
    private final TransactionItemDAO transactionItemDAO = new TransactionItemDAO();
    private final DecimalFormat formatter = new DecimalFormat("Rp #,###");

    @FXML
    public void initialize() {
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today.withDayOfMonth(1));
        endDatePicker.setValue(today);
        handleGenerate(null);
    }

    @FXML
    public void handleGenerate(ActionEvent event) {
        if (startDatePicker.getValue() == null || endDatePicker.getValue() == null) {
            showError("Please select both start and end dates.");
            return;
        }
        if (startDatePicker.getValue().isAfter(endDatePicker.getValue())) {
            showError("Start date cannot be after end date.");
            return;
        }

        String start = startDatePicker.getValue().toString();
        String end = endDatePicker.getValue().toString();

        double revenue = transactionDAO.getRevenueByDateRange(start, end);
        double expenses = transactionItemDAO.getTotalCostByDateRange(start, end);
        double profit = revenue - expenses;

        List<Transaction> completed = transactionDAO.getFilteredTransactions("Completed", start, end);

        revenueLabel.setText(formatter.format(revenue));
        expensesLabel.setText(formatter.format(expenses));
        profitLabel.setText(formatter.format(profit));
        profitLabel.getStyleClass().removeAll("metric-positive", "metric-warning");
        if (profit >= 0) {
            profitLabel.getStyleClass().add("metric-positive");
        } else {
            profitLabel.getStyleClass().add("metric-warning");
        }
        transactionCountLabel.setText(completed.size() + " completed transaction(s)");
        showSuccess("Report generated for " + start + " to " + end + ".");
    }

    @FXML
    public void handleBack(ActionEvent event) {
        JamuSyncApp.changeScene("/com/quantalabs/jamusync/fxml/OwnerDashboard.fxml", "JamuSync - Owner Dashboard");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("label-success");
        if (!messageLabel.getStyleClass().contains("label-error")) {
            messageLabel.getStyleClass().add("label-error");
        }
    }

    private void showSuccess(String message) {
        messageLabel.setText(message);
        messageLabel.getStyleClass().removeAll("label-error");
        if (!messageLabel.getStyleClass().contains("label-success")) {
            messageLabel.getStyleClass().add("label-success");
        }
    }
}
