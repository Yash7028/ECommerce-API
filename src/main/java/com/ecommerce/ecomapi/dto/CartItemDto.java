package com.ecommerce.ecomapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CartItemDto {
    private Long itemId;
    private String productId;
    private String productName;
    private String imageUrl;
    private BigDecimal discountedPrice;
    private Integer quantity;
    private BigDecimal totalAmount;
}
