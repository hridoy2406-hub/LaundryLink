package com.laundrylink.model;

public class Staff extends User {

    private String designation; // e.g. Washer, Ironer, Delivery
    private String shift;       // e.g. Morning, Evening

    public Staff(String fullName, String username, String password,
                 String email, String phone, String designation, String shift) {
        super(fullName, username, password, email, phone);
        this.designation = designation;
        this.shift = shift;
    }

    @Override
    public Role getRole() {
        return Role.STAFF;
    }

    @Override
    public String getDashboardTitle() {
        return "Staff Dashboard";
    }

    @Override
    public String getPermissionSummary() {
        return "View pending orders, process orders, update order status, use inventory";
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    @Override
    public String getSummary() {
        return super.getSummary() + " - " + designation + " (" + shift + " shift)";
    }
}