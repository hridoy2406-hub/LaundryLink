package com.laundrylink.controller;

import com.laundrylink.model.OrderStatus;
import com.laundrylink.model.Staff;
import javafx.scene.control.TabPane;

public class StaffDashboard extends BaseDashboard {

    public StaffDashboard(Staff staff) {
        super(staff);
        TabPane tabPane = new TabPane(
                new OrderQueueTab(staff, OrderStatus.PENDING),
                new OrderQueueTab(staff, OrderStatus.PROCESSING),
                new OrderQueueTab(staff, OrderStatus.READY),
                new BatchProcessTab(staff));
        Refreshable.enableAutoRefresh(tabPane);
        setCenter(tabPane);
    }
}