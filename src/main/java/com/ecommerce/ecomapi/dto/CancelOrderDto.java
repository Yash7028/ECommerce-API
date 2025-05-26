package com.ecommerce.ecomapi.dto;

import lombok.Data;

@Data
public class CancelOrderDto {
    private Long orderId;
    private String reasonMessage;
}
