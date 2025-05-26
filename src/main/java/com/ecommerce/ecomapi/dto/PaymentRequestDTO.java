package com.ecommerce.ecomapi.dto;

import lombok.Data;

@Data
public class PaymentRequestDTO {
    private Long amount; // In R$
    private String productName;
}
