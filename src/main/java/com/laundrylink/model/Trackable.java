package com.laundrylink.model;

/** Anything whose progress can be tracked through statuses. */
public interface Trackable {
    OrderStatus getStatus();

    void updateStatus(OrderStatus newStatus);
}