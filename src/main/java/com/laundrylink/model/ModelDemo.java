package com.laundrylink.model;

import com.laundrylink.model.Admin;
import com.laundrylink.model.BaseEntity;
import com.laundrylink.model.Billable;
import com.laundrylink.model.Customer;
import com.laundrylink.model.InventoryItem;
import com.laundrylink.model.Invoice;
import com.laundrylink.model.LaundryOrder;
import com.laundrylink.model.OrderItem;
import com.laundrylink.model.OrderStatus;
import com.laundrylink.model.ServiceType;
import com.laundrylink.model.Staff;
import com.laundrylink.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Console demo of the OOP model layer. Run this class directly. */
public class ModelDemo {

    public static void main(String[] args) {

        // ===== 1. Inheritance + abstract class + polymorphism =====
        Customer customer = new Customer("Rahim Uddin", "rahim", "1234",
                "rahim@mail.com", "01711111111", "House 12, Road 5");
        customer.setId(1);
        Staff staff = new Staff("Karim Hossain", "karim", "1234",
                "karim@mail.com", "01822222222", "Washer", "Morning");
        staff.setId(2);
        Admin admin = new Admin("Sara Akter", "sara", "admin123",
                "sara@mail.com", "01933333333", 3);
        admin.setId(3);

        List<User> users = List.of(customer, staff, admin);

        System.out.println("=== 1. Users: one User type, three different behaviours ===");
        for (User user : users) {
            System.out.println(user.getSummary());
            System.out.println("   Dashboard : " + user.getDashboardTitle());
            System.out.println("   Can do    : " + user.getPermissionSummary());
        }

        // ===== 2. Order, items, encapsulation and status tracking =====
        System.out.println();
        System.out.println("=== 2. Order with items ===");
        LaundryOrder order = new LaundryOrder(customer.getId(), LocalDate.now().plusDays(2), "Handle with care");
        order.setId(101);
        order.addItem(new OrderItem("Shirt", ServiceType.WASH_AND_IRON, 3));       // default price
        order.addItem(new OrderItem("Bed Sheet", ServiceType.WASH, 2, 80.0));      // custom price
        order.addItem(new OrderItem("Suit", ServiceType.DRY_CLEAN, 1, 350.0));

        for (OrderItem item : order.getItems()) {
            System.out.println("   " + item.getSummary());
        }
        System.out.println(order.getSummary());

        System.out.println("Status: " + order.getStatus());
        while (!order.getStatus().isFinal()) {
            order.updateStatus(order.getStatus().next());
            System.out.println("Status -> " + order.getStatus());
        }
        try {
            order.updateStatus(OrderStatus.PENDING);
        } catch (IllegalStateException e) {
            System.out.println("Blocked: " + e.getMessage());
        }

        // ===== 3. Interface polymorphism: Billable =====
        System.out.println();
        System.out.println("=== 3. Billable: item, order and invoice through one interface ===");
        Invoice invoice = new Invoice(order.getId(), order.calculateTotal(), 20.0);
        invoice.setId(1);

        List<Billable> billables = new ArrayList<>();
        billables.addAll(order.getItems());
        billables.add(order);
        billables.add(invoice);
        for (Billable billable : billables) {
            System.out.println("   " + billable.getClass().getSimpleName()
                    + " total = " + String.format("%.2f", billable.calculateTotal()));
        }
        invoice.markAsPaid();
        System.out.println(invoice.getSummary());

        // ===== 4. Inventory with business rules =====
        System.out.println();
        System.out.println("=== 4. Inventory ===");
        InventoryItem detergent = new InventoryItem("Detergent Powder", "kg", 10, 3);
        detergent.setId(1);
        System.out.println(detergent.getSummary());
        detergent.useStock(4);
        System.out.println(detergent.getSummary());
        detergent.useStock(3.5);
        System.out.println(detergent.getSummary());
        try {
            detergent.useStock(100);
        } catch (IllegalStateException e) {
            System.out.println("Blocked: " + e.getMessage());
        }

        // ===== 5. Everything is a BaseEntity =====
        System.out.println();
        System.out.println("=== 5. Every entity has getSummary() ===");
        List<BaseEntity> everything = new ArrayList<>();
        everything.addAll(users);
        everything.add(order);
        everything.addAll(order.getItems());
        everything.add(invoice);
        everything.add(detergent);
        for (BaseEntity entity : everything) {
            System.out.println("   " + entity.getSummary());
        }
    }
}