package com.laundrylink.model;

/**
 * Abstract parent of Customer, Staff and Admin.
 * You can NOT write "new User(...)". Only the subclasses can be created.
 */
public abstract class User extends BaseEntity {

    // Encapsulation: fields are private, accessed only through methods
    private String fullName;
    private String username;
    private String password;
    private String email;
    private String phone;

    protected User(String fullName, String username, String password, String email, String phone) {
        requireText(fullName, "Full name");
        requireText(username, "Username");
        requireText(password, "Password");
        this.fullName = fullName.trim();
        this.username = username.trim();
        this.password = password;
        this.email = email;
        this.phone = phone;
    }

    // ----- Abstract methods: every subclass MUST write its own version -----

    public abstract Role getRole();

    public abstract String getDashboardTitle();

    public abstract String getPermissionSummary();

    // ----- Shared behaviour for all users -----

    public boolean checkPassword(String input) {
        return password.equals(input);
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }

    // ----- Getters and setters -----

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        requireText(fullName, "Full name");
        this.fullName = fullName.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        requireText(username, "Username");
        this.username = username.trim();
    }

    public String getPassword() { // needed by the DAO when saving
        return password;
    }

    public void setPassword(String password) {
        requireText(password, "Password");
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String getSummary() {
        return "#" + getId() + " " + fullName + " [" + getRole() + "]";
    }

    @Override
    public String toString() {
        return getSummary();
    }
}