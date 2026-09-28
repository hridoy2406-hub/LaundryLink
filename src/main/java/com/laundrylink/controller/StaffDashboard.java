package com.laundrylink.controller;

import com.laundrylink.model.Staff;
import javafx.scene.control.Label;

public class StaffDashboard extends BaseDashboard {
    public StaffDashboard(Staff staff) {
        super(staff);
        setCenter(new Label("Staff dashboard - coming in Stage 8"));
    }
}
