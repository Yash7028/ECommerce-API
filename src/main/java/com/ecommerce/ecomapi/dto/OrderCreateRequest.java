package com.ecommerce.ecomapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {
    private AddressInput billingAddress;
    private AddressInput shippingAddress;
    private List<OrderProductInput> products;

    @Data
    public static class AddressInput {
        private Long addressId; // optional if existing
        private String street;
        private String city;
        private String state;
        private String zip;
        private String country;
        private String addressType;
    }

    @Data
    public static class OrderProductInput {
        private String productId;
        private int quantity;
    }
}
