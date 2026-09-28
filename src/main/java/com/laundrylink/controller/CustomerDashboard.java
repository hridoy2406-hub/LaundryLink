package com.laundrylink.controller;

import com.laundrylink.model.Customer;
import javafx.scene.control.TabPane;

public class CustomerDashboard extends BaseDashboard {

    public CustomerDashboard(Customer customer) {
        super(customer);
        TabPane tabPane = new TabPane(new MyOrdersTab(customer));
        Refreshable.enableAutoRefresh(tabPane);
        setCenter(tabPane);
    }
}
