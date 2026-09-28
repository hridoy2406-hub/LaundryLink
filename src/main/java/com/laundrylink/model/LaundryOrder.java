package com.laundrylink.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** A customer's laundry order. It owns a list of OrderItem objects. */
public class LaundryOrder extends BaseEntity implements Billable, Trackable {

    private int customerId;   // which customer placed it
    private int staffId;      // 0 = not assigned to any staff yet
    private LocalDate orderDate;
    private LocalDate pickupDate;
    private OrderStatus status;
    private String notes;
    private final List<OrderItem> items = new ArrayList<>();

    public LaundryOrder(int customerId, LocalDate pickupDate, String notes) {
        this.customerId = customerId;
        this.pickupDate = pickupDate;
        this.notes = notes;
        this.orderDate = LocalDate.now();
        this.status = OrderStatus.PENDING;
    }

    // ----- Items -----

    public void addItem(OrderItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        items.add(item);
    }

    public void removeItem(OrderItem item) {
        items.remove(item);
    }

    /** Read-only view: outside code cannot change the list directly (encapsulation). */
    public List<OrderItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public int getTotalQuantity() {
        int total = 0;
        for (OrderItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    // ----- Billable -----

    @Override
    public double calculateTotal() {
        double total = 0;
        for (OrderItem item : items) {
            total += item.calculateTotal();
        }
        return total;
    }

    // ----- Trackable -----

    @Override
    public OrderStatus getStatus() {
        return status;
    }

    @Override
    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null");
        }
        if (status.isFinal()) {
            throw new IllegalStateException("Order is already " + status + " and cannot be changed");
        }
        this.status = newStatus;
    }

    /** Raw setter used by the DAO when loading from the database. UI code should use updateStatus(). */
    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public boolean canBeCancelled() {
        return status == OrderStatus.PENDING;
    }

    public void cancel() {
        if (!canBeCancelled()) {
            throw new IllegalStateException("Only pending orders can be cancelled");
        }
        this.status = OrderStatus.CANCELLED;
    }

    // ----- Getters and setters -----

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getStaffId() {
        return staffId;
    }

    public void setStaffId(int staffId) {
        this.staffId = staffId;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String getSummary() {
        return "Order #" + getId() + " | Customer #" + customerId + " | " + status
                + " | " + items.size() + " item(s) | Total: " + String.format("%.2f", calculateTotal());
    }
}