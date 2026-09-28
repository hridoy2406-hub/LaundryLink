package com.laundrylink.controller;

import com.laundrylink.model.Admin;
import javafx.scene.control.Label;

public class AdminDashboard extends BaseDashboard {
    public AdminDashboard(Admin admin) {
        super(admin);
        setCenter(new Label("Admin dashboard - coming in Stage 9"));
    }
}
