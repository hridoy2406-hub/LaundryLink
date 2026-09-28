package com.laundrylink.task;

import com.laundrylink.dao.OrderDAO;
import com.laundrylink.model.LaundryOrder;
import com.laundrylink.model.OrderStatus;

import java.util.concurrent.Callable;

/** Processes one pending order in the background: PENDING -> PROCESSING -> READY. */
public class OrderProcessingTask implements Callable<String> {

    private final int orderId;
    private final int staffId;
    private final OrderDAO orderDAO = new OrderDAO();

    public OrderProcessingTask(int orderId, int staffId) {
        this.orderId = orderId;
        this.staffId = staffId;
    }

    @Override
    public String call() throws Exception {
        String worker = Thread.currentThread().getName();
        LaundryOrder order = orderDAO.findById(orderId);
        if (order == null) {
            return "Order #" + orderId + " not found";
        }
        if (order.getStatus() != OrderStatus.PENDING) {
            return "Order #" + orderId + " skipped (already " + order.getStatus() + ")";
        }
        orderDAO.updateStatus(orderId, OrderStatus.PROCESSING, staffId);
        Thread.sleep(3000); // pretend the washing takes time
        orderDAO.updateStatus(orderId, OrderStatus.READY, staffId);
        return "Order #" + orderId + " is READY (done by " + worker + ")";
    }
}