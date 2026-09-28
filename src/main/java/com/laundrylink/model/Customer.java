package com.laundrylink.model;

public class Customer extends User {

    private String address;

    public Customer(String fullName, String username, String password,
                    String email, String phone, String address) {
        super(fullName, username, password, email, phone);
        this.address = address;
    }

    @Override
    public Role getRole() {
        return Role.CUSTOMER;
    }

    @Override
    public String getDashboardTitle() {
        return "Customer Dashboard";
    }

    @Override
    public String getPermissionSummary() {
        return "Place orders, track order status, view order history and bills";
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String getSummary() {
        return super.getSummary() + " - " + address;
    }
}