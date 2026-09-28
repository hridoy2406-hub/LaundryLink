package com.laundrylink.util;

import com.laundrylink.dao.InventoryDAO;
import com.laundrylink.dao.InvoiceDAO;
import com.laundrylink.dao.OrderDAO;
import com.laundrylink.dao.UserDAO;
import com.laundrylink.database.DatabaseInitializer;
import com.laundrylink.model.InventoryItem;
import com.laundrylink.model.Invoice;
import com.laundrylink.model.LaundryOrder;
import com.laundrylink.model.OrderItem;
import com.laundrylink.model.OrderStatus;
import com.laundrylink.model.ServiceType;
import com.laundrylink.model.User;

import java.time.LocalDate;

/** Console test of all DAOs. Run this class directly. */
public class DaoDemo {

    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        UserDAO userDAO = new UserDAO();
        OrderDAO orderDAO = new OrderDAO();
        InvoiceDAO invoiceDAO = new InvoiceDAO();
        InventoryDAO inventoryDAO = new InventoryDAO();

        System.out.println("Admin login OK      : " + (userDAO.authenticate("admin", "admin123") != null));
        System.out.println("Wrong password fails: " + (userDAO.authenticate("admin", "wrong") == null));

        User customer = userDAO.findByUsername("customer1");
        User staff = userDAO.findByUsername("staff1");
        System.out.println("Loaded (polymorphism): " + customer.getSummary() + " / " + staff.getSummary());

        // CREATE
        LaundryOrder order = new LaundryOrder(customer.getId(), LocalDate.now().plusDays(1), "Demo order");
        order.addItem(new OrderItem("Shirt", ServiceType.WASH_AND_IRON, 3));
        order.addItem(new OrderItem("Suit", ServiceType.DRY_CLEAN, 1));
        orderDAO.insert(order);
        invoiceDAO.insert(new Invoice(order.getId(), order.calculateTotal(), 0));

        // READ
        System.out.println("History size: " + orderDAO.findByCustomerId(customer.getId()).size());
        System.out.println(orderDAO.findById(order.getId()).getSummary());

        // UPDATE
        orderDAO.updateStatus(order.getId(), OrderStatus.PROCESSING, staff.getId());
        System.out.println("After update : " + orderDAO.findById(order.getId()).getSummary());

        // Inventory usage
        InventoryItem detergent = inventoryDAO.findAll().get(0);
        System.out.println("Stock before : " + detergent.getSummary());
        inventoryDAO.recordUsage(order.getId(), detergent.getId(), staff.getId(), 2);
        System.out.println("Stock after  : " + inventoryDAO.findById(detergent.getId()).getSummary());

        // DELETE (cascade removes items and invoice)
        LaundryOrder temp = new LaundryOrder(customer.getId(), null, "temp");
        temp.addItem(new OrderItem("Sock", ServiceType.WASH, 1));
        orderDAO.insert(temp);
        invoiceDAO.insert(new Invoice(temp.getId(), temp.calculateTotal(), 0));
        orderDAO.delete(temp.getId());
        System.out.println("Deleted order found? " + (orderDAO.findById(temp.getId()) != null));
        System.out.println("Its invoice found?   " + (invoiceDAO.findByOrderId(temp.getId()) != null));
    }
}
