package com.laundrylink.task;

import com.laundrylink.dao.InventoryDAO;
import com.laundrylink.dao.OrderDAO;
import com.laundrylink.model.Customer;
import com.laundrylink.model.LaundryOrder;
import com.laundrylink.model.OrderStatus;
import com.laundrylink.model.User;
import javafx.application.Platform;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Runs in the background and sends a reminder text to the UI. */
public class ReminderTask implements Runnable {

    private final User user;
    private final Consumer<String> onMessage;
    private final OrderDAO orderDAO = new OrderDAO();
    private final InventoryDAO inventoryDAO = new InventoryDAO();

    public ReminderTask(User user, Consumer<String> onMessage) {
        this.user = user;
        this.onMessage = onMessage;
    }

    @Override
    public void run() {
        String message = buildMessage();
        if (message != null) {
            // UI can only be changed from the JavaFX thread
            Platform.runLater(() -> onMessage.accept(message));
        }
    }

    private String buildMessage() {
        List<String> parts = new ArrayList<>();
        if (user instanceof Customer customer) {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            for (LaundryOrder order : orderDAO.findByCustomerId(customer.getId())) {
                if (order.getStatus().isFinal() || order.getPickupDate() == null) continue;
                if (order.getStatus() == OrderStatus.READY) {
                    parts.add("Order #" + order.getId() + " is ready for pickup");
                } else if (!order.getPickupDate().isAfter(tomorrow)) {
                    parts.add("Order #" + order.getId() + " pickup date is " + order.getPickupDate());
                }
            }
        } else {
            int pending = orderDAO.findByStatus(OrderStatus.PENDING).size();
            int low = inventoryDAO.findLowStock().size();
            if (pending > 0) parts.add(pending + " pending order(s)");
            if (low > 0) parts.add(low + " low-stock item(s)");
        }
        return parts.isEmpty() ? null : "Reminder: " + String.join(" | ", parts);
    }
}