package com.ecommerce.ecomapi.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemDto {
    private Long id;
    private String productId;
    private String name;
    private int quantity;
    private BigDecimal totalPrice;
    private BigDecimal discountedPrice;
    private String mainImage;
    private List<String> additionalImages;
}
