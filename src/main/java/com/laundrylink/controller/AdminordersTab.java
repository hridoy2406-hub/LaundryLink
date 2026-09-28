package com.laundrylink.controller;

import com.laundrylink.dao.OrderDAO;
import com.laundrylink.dao.UserDAO;
import com.laundrylink.model.LaundryOrder;
import com.laundrylink.model.OrderStatus;
import com.laundrylink.model.User;
import com.laundrylink.util.AlertUtil;
import com.laundrylink.util.UiUtil;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

public class AdminOrdersTab extends Tab implements Refreshable {

    private final OrderDAO orderDAO = new OrderDAO();
    private final UserDAO userDAO = new UserDAO();
    private final TableView<LaundryOrder> table = new TableView<>();
    private final Map<Integer, String> names = new HashMap<>();
    private final ComboBox<OrderStatus> statusBox =
            new ComboBox<>(FXCollections.observableArrayList(OrderStatus.values()));

    public AdminOrdersTab() {
        super("Orders");
        setClosable(false);

        UiUtil.addColumn(table, "Order", o -> "#" + o.getId());
        UiUtil.addColumn(table, "Customer", o -> names.getOrDefault(o.getCustomerId(), "#" + o.getCustomerId()));
        UiUtil.addColumn(table, "Staff", o -> o.getStaffId() == 0 ? "-" : names.getOrDefault(o.getStaffId(), "#" + o.getStaffId()));
        UiUtil.addColumn(table, "Order Date", o -> o.getOrderDate().toString());
        UiUtil.addColumn(table, "Status", o -> o.getStatus().toString());
        UiUtil.addColumn(table, "Total", o -> UiUtil.money(o.calculateTotal()));
        UiUtil.bindColumnWidths(table, 0.10, 0.22, 0.20, 0.16, 0.17, 0.12);

        statusBox.setValue(OrderStatus.PROCESSING);
        Button updateBtn = new Button("Set Status");
        updateBtn.getStyleClass().add("primary-button");
        updateBtn.setOnAction(e -> {
            LaundryOrder order = selected();
            if (order != null) {
                orderDAO.updateStatus(order.getId(), statusBox.getValue(), null);
                refresh();
            }
        });
        Button deleteBtn = new Button("Delete Order");
        deleteBtn.getStyleClass().add("danger-button");
        deleteBtn.setOnAction(e -> {
            LaundryOrder order = selected();
            if (order != null && AlertUtil.confirm("Delete", "Delete order #" + order.getId() + " and its invoice?")) {
                orderDAO.delete(order.getId());
                refresh();
            }
        });
        Button refreshBtn = new Button("Refresh");
        refreshBtn.setOnAction(e -> refresh());

        VBox content = UiUtil.page(UiUtil.sectionTitle("All Orders"),
                new HBox(10, statusBox, updateBtn, deleteBtn, refreshBtn), table);
        VBox.setVgrow(table, Priority.ALWAYS);
        setContent(content);
        refresh();
    }

    @Override
    public void refresh() {
        names.clear();
        for (User u : userDAO.findAll()) names.put(u.getId(), u.getFullName());
        table.setItems(FXCollections.observableArrayList(orderDAO.findAll()));
    }

    private LaundryOrder selected() {
        LaundryOrder order = table.getSelectionModel().getSelectedItem();
        if (order == null) AlertUtil.error("No selection", "Select an order first.");
        return order;
    }
}