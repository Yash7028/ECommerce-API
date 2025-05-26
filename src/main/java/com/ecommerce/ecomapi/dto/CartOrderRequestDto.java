package com.ecommerce.ecomapi.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartOrderRequestDto {
    private OrderCreateRequest.AddressInput billingAddress;
    private OrderCreateRequest.AddressInput shippingAddress;
    private Long cartId;

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
}
