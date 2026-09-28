package com.laundrylink.model;

/** Anything that is kept in stock and can run low. */
public interface Stockable {
    void addStock(double amount);

    void useStock(double amount);

    boolean isLowStock();
}