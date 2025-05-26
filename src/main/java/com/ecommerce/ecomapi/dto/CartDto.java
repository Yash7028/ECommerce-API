package com.ecommerce.ecomapi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class CartDto {
    private Long cartId;
    private BigDecimal totalAmount;
    private List<CartItemDto> items;
    // Constructors, getters, setters
}

