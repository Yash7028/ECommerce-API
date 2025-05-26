package com.ecommerce.ecomapi.dto;

import lombok.Data;

@Data
public class UserPaymentDTO {
    private Long orderId;
//    private String id;
    private Long amountToPay;
}
