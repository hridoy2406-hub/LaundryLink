package com.laundrylink.model;

import java.time.LocalDate;

/** The bill of an order: subtotal + tax - discount. */
public class Invoice extends BaseEntity implements Billable {

    public static final double DEFAULT_TAX_RATE = 0.05; // 5%

    private int orderId;
    private LocalDate issueDate;
    private double subtotal;
    private double taxRate;   // 0.05 means 5%
    private double discount;  // fixed amount
    private boolean paid;

    // Constructor overloading: default tax rate
    public Invoice(int orderId, double subtotal, double discount) {
        this(orderId, subtotal, DEFAULT_TAX_RATE, discount);
    }

    public Invoice(int orderId, double subtotal, double taxRate, double discount) {
        this.orderId = orderId;
        setSubtotal(subtotal);
        setTaxRate(taxRate);
        setDiscount(discount);
        this.issueDate = LocalDate.now();
        this.paid = false;
    }

    public double calculateTaxAmount() {
        return subtotal * taxRate;
    }

    @Override
    public double calculateTotal() {
        double total = subtotal + calculateTaxAmount() - discount;
        return Math.max(total, 0);
    }

    public void markAsPaid() {
        this.paid = true;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        if (subtotal < 0) {
            throw new IllegalArgumentException("Subtotal cannot be negative");
        }
        this.subtotal = subtotal;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        if (taxRate < 0 || taxRate > 1) {
            throw new IllegalArgumentException("Tax rate must be between 0 and 1");
        }
        this.taxRate = taxRate;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        if (discount < 0) {
            throw new IllegalArgumentException("Discount cannot be negative");
        }
        this.discount = discount;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) { // used by the DAO when loading
        this.paid = paid;
    }

    @Override
    public String getSummary() {
        return "Invoice #" + getId() + " | Order #" + orderId + " | Total: "
                + String.format("%.2f", calculateTotal()) + " | " + (paid ? "PAID" : "UNPAID");
    }
}