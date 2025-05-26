package com.ecommerce.ecomapi.dto;

import lombok.Data;

@Data
public class PaymentIntentRequest {
    private Long orderId;
    private Long userId;
    private Long amount;
}
