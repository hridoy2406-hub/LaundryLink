package com.laundrylink.model;

public class Admin extends User {

    private int accessLevel; // 1 = normal admin ... 3 = super admin

    public Admin(String fullName, String username, String password,
                 String email, String phone, int accessLevel) {
        super(fullName, username, password, email, phone);
        setAccessLevel(accessLevel);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }

    @Override
    public String getDashboardTitle() {
        return "Admin Dashboard";
    }

    @Override
    public String getPermissionSummary() {
        return "Manage users, inventory, billing, reports and analytics";
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        if (accessLevel < 1 || accessLevel > 3) {
            throw new IllegalArgumentException("Access level must be between 1 and 3");
        }
        this.accessLevel = accessLevel;
    }

    @Override
    public String getSummary() {
        return super.getSummary() + " - access level " + accessLevel;
    }
}