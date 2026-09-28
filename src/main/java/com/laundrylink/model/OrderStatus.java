package com.laundrylink.model;

public enum OrderStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    READY("Ready for Pickup"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled");

    private final String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Next step in the normal flow. Final statuses stay the same. */
    public OrderStatus next() {
        return switch (this) {
            case PENDING -> PROCESSING;
            case PROCESSING -> READY;
            case READY -> DELIVERED;
            case DELIVERED, CANCELLED -> this;
        };
    }

    /** A final status can no longer be changed. */
    public boolean isFinal() {
        return this == DELIVERED || this == CANCELLED;
    }

    @Override
    public String toString() {
        return displayName;
    }
}