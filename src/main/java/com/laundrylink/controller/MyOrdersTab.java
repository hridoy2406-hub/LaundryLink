package com.laundrylink.controller;

import com.laundrylink.dao.InvoiceDAO;
import com.laundrylink.dao.OrderDAO;
import com.laundrylink.model.Customer;
import com.laundrylink.model.Invoice;
import com.laundrylink.model.LaundryOrder;
import com.laundrylink.model.OrderItem;
import com.laundrylink.model.OrderStatus;
import com.laundrylink.util.AlertUtil;
import com.laundrylink.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;

public class MyOrdersTab extends Tab implements Refreshable {

    private final Customer customer;
    private final OrderDAO orderDAO = new OrderDAO();
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final TableView<LaundryOrder> table = new TableView<>();
    private final Label totalOrders = new Label("0");
    private final Label activeOrders = new Label("0");
    private final Label orderValue = new Label("0.00 Tk");

    public MyOrdersTab(Customer customer) {
        super("My Orders");
        this.customer = customer;
        setClosable(false);

        UiUtil.addColumn(table, "Order", o -> "#" + o.getId());
        UiUtil.addColumn(table, "Order Date", o -> o.getOrderDate().toString());
        UiUtil.addColumn(table, "Pickup Date", o -> o.getPickupDate() == null ? "-" : o.getPickupDate().toString());
        UiUtil.addColumn(table, "Pieces", o -> String.valueOf(o.getTotalQuantity()));
        UiUtil.addColumn(table, "Status", o -> o.getStatus().toString());
        UiUtil.addColumn(table, "Total", o -> UiUtil.money(o.calculateTotal()));
        UiUtil.bindColumnWidths(table, 0.10, 0.19, 0.19, 0.12, 0.20, 0.17);
        table.setPlaceholder(new Label("No orders yet"));

        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());
        Button billBtn = new Button("View Bill");
        billBtn.getStyleClass().add("primary-button");
        billBtn.setOnAction(e -> viewBill());
        Button cancelBtn = new Button("Cancel Order");
        cancelBtn.getStyleClass().add("danger-button");
        cancelBtn.setOnAction(e -> cancelOrder());

        VBox content = UiUtil.page(
                UiUtil.sectionTitle("Order History"),
                UiUtil.cardGrid(3,
                        UiUtil.statCard("Total Orders", totalOrders),
                        UiUtil.statCard("Active Orders", activeOrders),
                        UiUtil.statCard("Order Value (before tax)", orderValue)),
                new HBox(10, refreshBtn, billBtn, cancelBtn),
                table);
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(content);
        refresh();
    }

    @Override
    public void refresh() {
        List<LaundryOrder> orders = orderDAO.findByCustomerId(customer.getId());
        table.setItems(FXCollections.observableArrayList(orders));
        totalOrders.setText(String.valueOf(orders.size()));
        activeOrders.setText(String.valueOf(orders.stream().filter(o -> !o.getStatus().isFinal()).count()));
        orderValue.setText(UiUtil.money(orders.stream()
                .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(LaundryOrder::calculateTotal).sum()));
    }

    private void viewBill() {
        LaundryOrder order = table.getSelectionModel().getSelectedItem();
        if (order == null) {
            AlertUtil.error("No selection", "Please select an order first.");
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (OrderItem item : order.getItems()) sb.append(item.getSummary()).append("\n");
        Invoice invoice = invoiceDAO.findByOrderId(order.getId());
        if (invoice != null) {
            sb.append("\nSubtotal : ").append(UiUtil.money(invoice.getSubtotal()))
                    .append("\nTax      : ").append(UiUtil.money(invoice.calculateTaxAmount()))
                    .append("\nDiscount : ").append(UiUtil.money(invoice.getDiscount()))
                    .append("\nTOTAL    : ").append(UiUtil.money(invoice.calculateTotal()))
                    .append("\nPayment  : ").append(invoice.isPaid() ? "PAID" : "UNPAID");
        }
        AlertUtil.info("Bill - Order #" + order.getId(), sb.toString());
    }

    private void cancelOrder() {
        LaundryOrder order = table.getSelectionModel().getSelectedItem();
        if (order == null) {
            AlertUtil.error("No selection", "Please select an order first.");
            return;
        }
        if (!order.canBeCancelled()) {
            AlertUtil.error("Cannot cancel", "Only pending orders can be cancelled.");
            return;
        }
        if (AlertUtil.confirm("Cancel order", "Cancel order #" + order.getId() + "?")) {
            orderDAO.updateStatus(order.getId(), OrderStatus.CANCELLED, null);
            refresh();
        }
    }
}
