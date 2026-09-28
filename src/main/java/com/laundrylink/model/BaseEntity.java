package com.laundrylink.model;

/**
 * Parent of every model class. Holds the database id
 * so we don't repeat it in every class.
 */
public abstract class BaseEntity {

    private int id; // 0 = not saved to the database yet

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isNew() {
        return id == 0;
    }

    /** Every entity must describe itself in one line. */
    public abstract String getSummary();
}