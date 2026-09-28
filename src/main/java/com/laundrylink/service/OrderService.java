package com.laundrylink.service;

import com.laundrylink.dao.InvoiceDAO;
import com.laundrylink.dao.OrderDAO;
import com.laundrylink.model.Invoice;
import com.laundrylink.model.LaundryOrder;

/** Business logic of placing an order and calculating its bill. */
public class OrderService {

    public static final double DISCOUNT_THRESHOLD = 500.0; // subtotal at or above this gets a discount
    public static final double DISCOUNT_RATE = 0.10;       // 10%

    private final OrderDAO orderDAO = new OrderDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();

    /** Bill preview for the order form (not saved). */
    public Invoice previewInvoice(double subtotal) {
        return buildInvoice(0, subtotal);
    }

    /** Saves the order with its items and creates its invoice automatically. */
    public boolean placeOrder(LaundryOrder order) {
        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("Add at least one item");
        }
        if (!orderDAO.insert(order)) {
            return false;
        }
        return invoiceDAO.insert(buildInvoice(order.getId(), order.calculateTotal()));
    }

    private Invoice buildInvoice(int orderId, double subtotal) {
        double discount = subtotal >= DISCOUNT_THRESHOLD ? subtotal * DISCOUNT_RATE : 0;
        return new Invoice(orderId, subtotal, Invoice.DEFAULT_TAX_RATE, discount);
    }
}