package com.laundrylink.model;

public enum ServiceType {
    WASH("Wash", 30.0),
    WASH_AND_IRON("Wash & Iron", 50.0),
    IRON("Iron Only", 20.0),
    DRY_CLEAN("Dry Clean", 120.0);

    private final String displayName;
    private final double basePrice; // default price per item (Taka)

    ServiceType(String displayName, double basePrice) {
        this.displayName = displayName;
        this.basePrice = basePrice;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getBasePrice() {
        return basePrice;
    }

    @Override
    public String toString() {
        return displayName;
    }
}