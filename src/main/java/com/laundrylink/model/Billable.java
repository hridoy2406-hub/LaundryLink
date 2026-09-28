package com.laundrylink.model;

/** Anything that has a money amount: an item, an order, an invoice. */
public interface Billable {
    double calculateTotal();
}