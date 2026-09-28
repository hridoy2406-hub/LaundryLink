package com.laundrylink.controller;

import com.laundrylink.model.Customer;
import javafx.scene.control.TabPane;

public class CustomerDashboard extends BaseDashboard {

    public CustomerDashboard(Customer customer) {
        super(customer);
        TabPane tabPane = new TabPane();
        MyOrdersTab ordersTab = new MyOrdersTab(customer);
        NewOrderTab newOrderTab = new NewOrderTab(customer, () -> tabPane.getSelectionModel().select(ordersTab));
        tabPane.getTabs().addAll(ordersTab, newOrderTab, new WeatherTab());
        Refreshable.enableAutoRefresh(tabPane);
        setCenter(tabPane);
    }
}