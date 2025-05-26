package com.ecommerce.ecomapi.enums;

public enum OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    public static OrderStatus fromStringIgnoreCase(String value) {
        for (OrderStatus orderStatus : OrderStatus.values()) {
            if (orderStatus.name().equalsIgnoreCase(value)) {
                return orderStatus;
            }
        }
        throw new IllegalArgumentException("Invalid order status: " + value);
    }
}
