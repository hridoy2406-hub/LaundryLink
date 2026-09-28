package com.laundrylink.model;

/** A laundry supply kept in stock, e.g. detergent, softener, hangers. */
public class InventoryItem extends BaseEntity implements Stockable {

    private String name;
    private String unit;          // kg, liter, piece...
    private double quantity;
    private double reorderLevel;  // at or below this = low stock

    public InventoryItem(String name, String unit, double quantity, double reorderLevel) {
        setName(name);
        this.unit = unit;
        setQuantity(quantity);
        setReorderLevel(reorderLevel);
    }

    @Override
    public void addStock(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to add must be greater than 0");
        }
        quantity += amount;
    }

    @Override
    public void useStock(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to use must be greater than 0");
        }
        if (amount > quantity) {
            throw new IllegalStateException("Not enough stock: only "
                    + String.format("%.1f", quantity) + " " + unit + " of " + name + " left");
        }
        quantity -= amount;
    }

    @Override
    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Inventory item name cannot be empty");
        }
        this.name = name.trim();
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = quantity;
    }

    public double getReorderLevel() {
        return reorderLevel;
    }

    public void setReorderLevel(double reorderLevel) {
        if (reorderLevel < 0) {
            throw new IllegalArgumentException("Reorder level cannot be negative");
        }
        this.reorderLevel = reorderLevel;
    }

    @Override
    public String getSummary() {
        return String.format("%s: %.1f %s%s", name, quantity, unit, isLowStock() ? " [LOW STOCK]" : "");
    }
}