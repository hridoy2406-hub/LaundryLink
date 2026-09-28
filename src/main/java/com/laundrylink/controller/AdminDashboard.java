package com.laundrylink.controller;

import com.laundrylink.model.Admin;
import javafx.scene.control.TabPane;

public class AdminDashboard extends BaseDashboard {

    public AdminDashboard(Admin admin) {
        super(admin);
        TabPane tabPane = new TabPane(new UserManagementTab(), new InventoryTab(), new BillingTab(),
                new AdminOrdersTab(), new ReportsTab());
        Refreshable.enableAutoRefresh(tabPane);
        setCenter(tabPane);
    }
}