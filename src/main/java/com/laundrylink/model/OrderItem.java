package com.laundrylink.model;

/** One line of an order, e.g. "3 x Shirt (Wash & Iron)". */
public class OrderItem extends BaseEntity implements Billable {

    private int orderId; // links this item to its order (foreign key later)
    private String itemName;
    private ServiceType serviceType;
    private int quantity;
    private double unitPrice;

    public OrderItem(String itemName, ServiceType serviceType, int quantity, double unitPrice) {
        setItemName(itemName);
        setServiceType(serviceType);
        setQuantity(quantity);
        setUnitPrice(unitPrice);
    }

    // Constructor overloading: uses the default price of the service
    public OrderItem(String itemName, ServiceType serviceType, int quantity) {
        this(itemName, serviceType, quantity, serviceType.getBasePrice());
    }

    @Override
    public double calculateTotal() {
        return quantity * unitPrice;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        if (itemName == null || itemName.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be empty");
        }
        this.itemName = itemName.trim();
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        if (serviceType == null) {
            throw new IllegalArgumentException("Service type is required");
        }
        this.serviceType = serviceType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Unit price cannot be negative");
        }
        this.unitPrice = unitPrice;
    }

    @Override
    public String getSummary() {
        return quantity + " x " + itemName + " (" + serviceType + ") @ "
                + String.format("%.2f", unitPrice) + " = " + String.format("%.2f", calculateTotal());
    }
}